package me.gravitinos.aigame.server.packet.handler;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.packet.PacketInOutPing;
import me.gravitinos.aigame.server.GameServer;
import me.gravitinos.aigame.server.player.ServerPlayer;

public class PacketHandlerPing implements PacketHandlerServer {
    @Override
    public void handlePacket(ServerPlayer player, Packet packet, GameServer server) {
        player.sendMessage("Your ping is: " + (System.currentTimeMillis() - ((PacketInOutPing)packet).ms1));
    }
}
