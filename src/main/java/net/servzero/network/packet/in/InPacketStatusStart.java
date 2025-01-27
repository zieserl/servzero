package net.servzero.network.packet.in;

import net.servzero.network.packet.Packet;
import net.servzero.network.packet.handler.InPacketStatusHandler;
import net.servzero.network.packet.serialization.PacketDataSerializer;

public class InPacketStatusStart implements Packet<InPacketStatusHandler> {
    @Override
    public void read(PacketDataSerializer serializer) {

    }

    @Override
    public void write(PacketDataSerializer serializer) {

    }

    @Override
    public void handle(InPacketStatusHandler handler) {
        handler.handle(this);
    }
}
