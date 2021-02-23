package net.servzero.network.protocol;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import net.servzero.logger.Logger;
import net.servzero.network.packet.Packet;
import net.servzero.network.packet.in.InPacketHandshakeSetProtocol;
import net.servzero.network.packet.in.InPacketLoginStart;
import net.servzero.network.packet.in.InPacketStatusPing;
import net.servzero.network.packet.in.InPacketStatusStart;
import net.servzero.network.packet.out.OutPacketLoginSuccess;
import net.servzero.network.packet.out.OutPacketStatusPong;
import net.servzero.network.packet.out.OutPacketStatusResponse;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public enum EnumProtocol {
    HANDSHAKING(-1) {{
        this.addPacket(EnumProtocolDirection.TO_SERVER, InPacketHandshakeSetProtocol.class);
    }},
    PLAY(0),
    STATUS(1) {{
        this.addPacket(EnumProtocolDirection.TO_SERVER, InPacketStatusStart.class);
        this.addPacket(EnumProtocolDirection.TO_CLIENT, OutPacketStatusResponse.class);
        this.addPacket(EnumProtocolDirection.TO_SERVER, InPacketStatusPing.class);
        this.addPacket(EnumProtocolDirection.TO_CLIENT, OutPacketStatusPong.class);
    }},
    LOGIN(2) {{
        this.addPacket(EnumProtocolDirection.TO_SERVER, InPacketLoginStart.class);
        this.addPacket(EnumProtocolDirection.TO_CLIENT, OutPacketLoginSuccess.class);
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

    protected EnumProtocol addPacket(EnumProtocolDirection direction, Class<? extends Packet<?>> packet) {
        BiMap<Integer, Class<? extends Packet<?>>> map = this.packets.computeIfAbsent(direction, k -> HashBiMap.create());

        if (map.containsValue(packet)) {
            String error = "Could not register packet " + packet.getSimpleName() + ": already existing as id: " + map.inverse().get(packet);
            Logger.error(error);
            throw new IllegalArgumentException(error);
        } else {
            map.put(map.size(), packet);
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
