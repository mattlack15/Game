package me.gravitinos.aigame.server.packet.handler;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.packet.PacketInPositionConfirmation;
import me.gravitinos.aigame.server.GameServer;
import me.gravitinos.aigame.server.player.ServerPlayer;

public class PacketHandlerPositionConfirmation implements PacketHandlerServer {
    @Override
    public void handlePacket(ServerPlayer player, Packet packet, GameServer server) {
        PacketInPositionConfirmation posConf = (PacketInPositionConfirmation) packet;
        player.confirmTp(posConf.pId);
    }
}
