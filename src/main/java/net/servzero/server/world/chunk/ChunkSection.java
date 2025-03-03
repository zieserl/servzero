package net.servzero.server.world.chunk;

import net.servzero.network.packet.serialization.ISerializable;
import net.servzero.network.packet.serialization.PacketDataSerializer;
import net.servzero.server.world.block.Block;
import net.servzero.server.world.block.BlockState;
import net.servzero.server.world.block.Blocks;
import net.servzero.server.world.block.Position;

import java.util.Arrays;

public class ChunkSection implements ISerializable<PacketDataSerializer> {
    private static final int BITS_PER_BLOCK = 13;

    private final Chunk parent;
    private int y;
    private final Block[][][] blocks = new Block[16][16][16];
    private final Block[][][] emptyComparisonBlocks = new Block[16][16][16];

    public ChunkSection(Chunk parent, int sectionY) {
        this.parent = parent;
        this.y = sectionY;
    }

    public Chunk getParent() {
        return parent;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    private synchronized Block getBlockAtSectionCoordinate(int x, int y, int z) {
        Block block = blocks[x][y][z];
        if (block == null) {
            int chunkX = this.parent.getX();
            int chunkZ = this.parent.getZ();
            int chunkStartX = chunkX * 16;
            int chunkStartZ = chunkZ * 16;
            blocks[x][y][z] = block = new Block(parent.getWorld(), Position.get(chunkStartX + x, y, chunkStartZ + z), Blocks.AIR);
        }
        return block;
    }

    private synchronized void setBlockAtSectionCoordinate(int x, int y, int z, BlockState state) {
        Block block = blocks[x][y][z];
        if (block == null) {
            int chunkX = this.parent.getX();
            int chunkZ = this.parent.getZ();
            int chunkStartX = chunkX * 16;
            int chunkStartZ = chunkZ * 16;
            blocks[x][y][z] = block = new Block(parent.getWorld(), Position.get(chunkStartX + x, y, chunkStartZ + z), state);
            block.update();
        } else {
            block.setType(state);
        }
    }

    public void setBlock(Position coord, BlockState blockState) {
        setBlockAtSectionCoordinate(coord.getX(), coord.getY(), coord.getZ(), blockState);
    }

    public Block getBlockAt(Position position) {
        int originalX = position.getX();
        int originalY = position.getY();
        int originalZ = position.getZ();

        int sectionX = checkNegative(originalX % 16);
        int sectionY = checkNegative(originalY % 16);
        int sectionZ = checkNegative(originalZ % 16);

        return getBlockAtSectionCoordinate(sectionX, sectionY, sectionZ);
    }

    private int checkNegative(int coord) {
        if (coord < 0) {
            return 16 + coord;
        }
        return coord;
    }

    @Override
    public void write(PacketDataSerializer serializer) {
        serializer.writeByte(BITS_PER_BLOCK);
        serializer.writeVarInt(0);

        long[] data = new long[16 * 16 * 16 * BITS_PER_BLOCK / 64];

        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    int blockNumber = (((y * 16) + z) * 16) + x;
                    int startLong = (blockNumber * BITS_PER_BLOCK) / 64;
                    int startOffset = (blockNumber * BITS_PER_BLOCK) % 64;
                    int endLong = ((blockNumber + 1) * BITS_PER_BLOCK - 1) / 64;

                    Block block = getBlockAtSectionCoordinate(x, y, z);
                    int value = (block == null ? Blocks.AIR : block.getState()).toGlobalId();

                    data[startLong] |= ((long) value << startOffset);

                    if (startLong != endLong) {
                        data[endLong] = (value >> (64 - startOffset));
                    }
                }
            }
        }

        serializer.writeLongArray(data);

        // TODO: Add real block light

        for (int x = 0; x < 16; x += 2) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    int light = 15;
                    serializer.writeByte(light | (light << 4));
                }
            }
        }

        // TODO: Add real sky light

        for (int x = 0; x < 16; x += 2) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    int light = 15;
                    serializer.writeByte(light | (light << 4));
                }
            }
        }
    }

    public boolean hasBlocks() {
        return !Arrays.deepEquals(this.blocks, this.emptyComparisonBlocks);
    }
}
