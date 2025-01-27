package net.servzero.network.packet.in;

import net.servzero.network.packet.Packet;
import net.servzero.network.packet.handler.AbstractInPacketPlayHandler;
import net.servzero.network.packet.serialization.PacketDataSerializer;

import java.io.IOException;

public class InPacketCloseWindow implements Packet<AbstractInPacketPlayHandler> {
    private short windowId;

    @Override
    public void read(PacketDataSerializer serializer) throws IOException {
        this.windowId = serializer.readUnsignedByte();
    }

    @Override
    public void write(PacketDataSerializer serializer) throws IOException {

    }

    @Override
    public void handle(AbstractInPacketPlayHandler handler) {
        handler.handleCloseWindow(this);
    }

    public short getWindowId() {
        return windowId;
    }
}
