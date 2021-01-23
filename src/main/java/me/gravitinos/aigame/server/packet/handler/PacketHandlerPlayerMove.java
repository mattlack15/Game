package me.gravitinos.aigame.server.packet.handler;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.packet.PacketInPlayerMove;
import me.gravitinos.aigame.common.util.Vector;
import me.gravitinos.aigame.server.GameServer;
import me.gravitinos.aigame.server.player.ServerPlayer;

public class PacketHandlerPlayerMove implements PacketHandlerServer {
    @Override
    public void handlePacket(ServerPlayer player, Packet packet, GameServer server) {
        PacketInPlayerMove move = (PacketInPlayerMove) packet;
        Vector movement = move.movement;
        Vector pos = player.getPosition().add(movement);
        player.setPosition(pos);
    }
}
