package net.servzero.network.packet.handler;

import net.servzero.network.NetworkHandler;
import net.servzero.network.packet.Packet;
import net.servzero.network.packet.in.InPacketStatusPing;
import net.servzero.network.packet.in.InPacketStatusStart;
import net.servzero.network.packet.out.OutPacketStatusPong;
import net.servzero.network.packet.out.OutPacketStatusResponse;
import net.servzero.network.ping.PingResponse;

import java.util.ArrayList;
import java.util.UUID;

public class InPacketStatusHandler extends AbstractInPacketStatusHandler {
    public InPacketStatusHandler(NetworkHandler networkHandler) {
        super(networkHandler);
    }

    @Override
    public void handle(InPacketStatusStart packet) {
        PingResponse response = new PingResponse(
                new PingResponse.VersionInfo("1.0.0", this.networkHandler.getProtocolVersion()),
                new PingResponse.PlayerInfo(100, 47, new ArrayList<>() {{
                    add(new PingResponse.PlayerInfoItem("Name1", UUID.randomUUID()));
                    add(new PingResponse.PlayerInfoItem("Name2", UUID.randomUUID()));
                    add(new PingResponse.PlayerInfoItem("Name3", UUID.randomUUID()));
                }}),
                new PingResponse.Description("Not A Minecraft Server"),
                 ""
        );
        networkHandler.sendPacket(new OutPacketStatusResponse(response));
    }

    @Override
    public void handle(InPacketStatusPing packet) {
        this.networkHandler.sendPacket(new OutPacketStatusPong(packet.getSentTime()));
        this.networkHandler.close();
    }

    @Override
    public void handle(Packet<?> packet) {

    }
}
