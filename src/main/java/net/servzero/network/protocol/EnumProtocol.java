package net.servzero.network.protocol;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import net.servzero.logger.Logger;
import net.servzero.network.packet.Packet;
import net.servzero.network.packet.in.*;
import net.servzero.network.packet.in.player.*;
import net.servzero.network.packet.out.*;
import net.servzero.network.packet.out.entity.*;
import net.servzero.network.packet.out.player.OutPacketPlayerPositionLook;
import net.servzero.network.packet.out.player.OutPacketSetSlot;
import net.servzero.network.packet.out.player.OutPacketSoundEffect;
import net.servzero.network.packet.out.player.OutPacketSpawnPlayer;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public enum EnumProtocol {
    HANDSHAKING(-1) {{
        this.addPacket(0x00, EnumProtocolDirection.TO_SERVER, InPacketHandshakeSetProtocol.class);
    }},
    PLAY(0) {{
        this.addPacket(0x00, EnumProtocolDirection.TO_SERVER, InPacketTeleportConfirm.class);
        this.addPacket(0x02, EnumProtocolDirection.TO_SERVER, InPacketChatMessage.class);
        this.addPacket(0x04, EnumProtocolDirection.TO_SERVER, InPacketClientSettings.class);
        this.addPacket(0x08, EnumProtocolDirection.TO_SERVER, InPacketCloseWindow.class);
        this.addPacket(0x09, EnumProtocolDirection.TO_SERVER, InPacketPluginMessage.class);
        this.addPacket(0x0B, EnumProtocolDirection.TO_SERVER, InPacketKeepAlive.class);
        this.addPacket(0x0C, EnumProtocolDirection.TO_SERVER, InPacketPlayer.class);
        this.addPacket(0x0D, EnumProtocolDirection.TO_SERVER, InPacketPlayerPosition.class);
        this.addPacket(0x0E, EnumProtocolDirection.TO_SERVER, InPacketPlayerPositionLook.class);
        this.addPacket(0x0F, EnumProtocolDirection.TO_SERVER, InPacketPlayerLook.class);
        this.addPacket(0x14, EnumProtocolDirection.TO_SERVER, InPacketPlayerDig.class);
        this.addPacket(0x15, EnumProtocolDirection.TO_SERVER, InPacketEntityAction.class);
        this.addPacket(0x1A, EnumProtocolDirection.TO_SERVER, InPacketHeldItemChange.class);
        this.addPacket(0x1D, EnumProtocolDirection.TO_SERVER, InPacketAnimation.class);
        this.addPacket(0x1F, EnumProtocolDirection.TO_SERVER, InPacketPlayerBlockPlace.class);
        this.addPacket(0x05, EnumProtocolDirection.TO_CLIENT, OutPacketSpawnPlayer.class);
        this.addPacket(0x06, EnumProtocolDirection.TO_CLIENT, OutPacketAnimation.class);
        this.addPacket(0x0B, EnumProtocolDirection.TO_CLIENT, OutPacketBlockChange.class);
        this.addPacket(0x0D, EnumProtocolDirection.TO_CLIENT, OutPacketDifficulty.class);
        this.addPacket(0x0F, EnumProtocolDirection.TO_CLIENT, OutPacketChatMessage.class);
        this.addPacket(0x16, EnumProtocolDirection.TO_CLIENT, OutPacketSetSlot.class);
        this.addPacket(0x1A, EnumProtocolDirection.TO_CLIENT, OutPacketDisconnect.class);
        this.addPacket(0x1B, EnumProtocolDirection.TO_CLIENT, OutPacketEntityStatus.class);
        this.addPacket(0x1F, EnumProtocolDirection.TO_CLIENT, OutPacketKeepAlive.class);
        this.addPacket(0x20, EnumProtocolDirection.TO_CLIENT, OutPacketChunkData.class);
        this.addPacket(0x23, EnumProtocolDirection.TO_CLIENT, OutPacketJoinGame.class);
        this.addPacket(0x25, EnumProtocolDirection.TO_CLIENT, OutPacketEntity.class);
        this.addPacket(0x26, EnumProtocolDirection.TO_CLIENT, OutPacketEntityRelativeMove.class);
        this.addPacket(0x27, EnumProtocolDirection.TO_CLIENT, OutPacketEntityRelativeMoveLook.class);
        this.addPacket(0x28, EnumProtocolDirection.TO_CLIENT, OutPacketEntityLook.class);
        this.addPacket(0x2C, EnumProtocolDirection.TO_CLIENT, OutPacketPlayerAbilities.class);
        this.addPacket(0x2E, EnumProtocolDirection.TO_CLIENT, OutPacketPlayerListItem.class);
        this.addPacket(0x2F, EnumProtocolDirection.TO_CLIENT, OutPacketPlayerPositionLook.class);
        this.addPacket(0x32, EnumProtocolDirection.TO_CLIENT, OutPacketDestroyEntities.class);
        this.addPacket(0x36, EnumProtocolDirection.TO_CLIENT, OutPacketEntityHeadLook.class);
        this.addPacket(0x3A, EnumProtocolDirection.TO_CLIENT, OutPacketHeldItemChange.class);
        this.addPacket(0x3C, EnumProtocolDirection.TO_CLIENT, OutPacketEntityMetadata.class);
        this.addPacket(0x49, EnumProtocolDirection.TO_CLIENT, OutPacketSoundEffect.class);
        this.addPacket(0x4C, EnumProtocolDirection.TO_CLIENT, OutPacketEntityTeleport.class);
    }},
    STATUS(1) {{
        this.addPacket(0x00, EnumProtocolDirection.TO_SERVER, InPacketStatusStart.class);
        this.addPacket(0x01, EnumProtocolDirection.TO_SERVER, InPacketStatusPing.class);
        this.addPacket(0x00, EnumProtocolDirection.TO_CLIENT, OutPacketStatusResponse.class);
        this.addPacket(0x01, EnumProtocolDirection.TO_CLIENT, OutPacketStatusPong.class);
    }},
    LOGIN(2) {{
        this.addPacket(0x00, EnumProtocolDirection.TO_SERVER, InPacketLoginStart.class);
        this.addPacket(0x00, EnumProtocolDirection.TO_CLIENT, OutPacketLoginDisconnect.class);
        this.addPacket(0x02, EnumProtocolDirection.TO_CLIENT, OutPacketLoginSuccess.class);
    }};

    public static EnumProtocol getById(int id) {
        return Arrays.stream(values()).filter(enumProtocol -> enumProtocol.id == id).findFirst().orElse(null);
    }

    public static EnumProtocol getByPacket(Packet<?> packet) {
        return protocols.get(packet.getClass());
    }

    private static final Map<Class<? extends Packet<?>>, EnumProtocol> protocols = Maps.newHashMap();
    private final int id;
    private final Map<EnumProtocolDirection, BiMap<Integer, Class<? extends Packet<?>>>> packets;

    EnumProtocol(int id) {
        this.id = id;
        this.packets = Maps.newEnumMap(EnumProtocolDirection.class);
    }

    public int getId() {
        return id;
    }

    public Optional<Packet<?>> getPacket(EnumProtocolDirection direction, int id) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<? extends Packet<?>> cls = null;
        if (this.packets.containsKey(direction)) {
            cls = this.packets.get(direction).get(id);
        }
        return Optional.ofNullable(cls == null ? null : cls.getDeclaredConstructor().newInstance());
    }

    public int getPacketId(EnumProtocolDirection direction, Packet<?> packet) {
        return this.packets.get(direction).inverse().getOrDefault(packet.getClass(), -1);
    }

    protected EnumProtocol addPacket(int id, EnumProtocolDirection direction, Class<? extends Packet<?>> packet) {
        BiMap<Integer, Class<? extends Packet<?>>> map = this.packets.computeIfAbsent(direction, k -> HashBiMap.create());

        if (map.containsValue(packet)) {
            String error = "Could not register packet " + packet.getSimpleName() + ": already existing as id: " + map.inverse().get(packet);
            Logger.error(error);
            throw new IllegalArgumentException(error);
        } else {
//            map.put(map.size(), packet);
            map.put(id, packet);
            return this;
        }
    }

    static {
        Arrays.stream(values()).forEach(enumProtocol -> {
            enumProtocol.packets.forEach((direction, integerClassBiMap) -> {
                integerClassBiMap.forEach((integer, packetClass) -> {
                    if (protocols.containsKey(packetClass)) {
                        throw new Error("Packet " + packetClass.getSimpleName() + " is already assigned to protocol " + enumProtocol);
                    }

                    protocols.put(packetClass, enumProtocol);
                });
            });
        });
    }
}
