package me.gravitinos.aigame.server.packet.handler;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.packet.PacketInOutChatMessage;
import me.gravitinos.aigame.server.GameServer;
import me.gravitinos.aigame.server.player.ServerPlayer;

public class PacketHandlerChatMessage implements PacketHandlerServer {
    @Override
    public void handlePacket(ServerPlayer player, Packet packet, GameServer server) {
        String message = server.onChatMessage(player, ((PacketInOutChatMessage)packet).message);
        if(message == null)
            return;
        server.world.getPlayers().forEach(p -> p.getConnection().sendPacket(new PacketInOutChatMessage(message)));
        System.out.println("Chat Message: " + message);
    }
}
