package me.gravitinos.aigame.server.packet.handler;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.server.GameServer;
import me.gravitinos.aigame.server.player.ServerPlayer;

public class PacketHandlerDisconnect implements PacketHandlerServer {
    @Override
    public void handlePacket(ServerPlayer player, Packet packet, GameServer server) {
        server.handleDisconnect(player);
    }
}
