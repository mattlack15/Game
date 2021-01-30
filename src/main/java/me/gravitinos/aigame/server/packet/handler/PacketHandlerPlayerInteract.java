package me.gravitinos.aigame.server.packet.handler;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.packet.PacketInPlayerInteract;
import me.gravitinos.aigame.common.util.Vector;
import me.gravitinos.aigame.server.GameServer;
import me.gravitinos.aigame.server.event.EventSubscriptions;
import me.gravitinos.aigame.server.event.events.PlayerInteractEvent;
import me.gravitinos.aigame.server.player.ServerPlayer;

public class PacketHandlerPlayerInteract implements PacketHandlerServer {
    @Override
    public void handlePacket(ServerPlayer player, Packet packet, GameServer server) {
        PacketInPlayerInteract playerInteract = (PacketInPlayerInteract) packet;
        PlayerInteractEvent event = new PlayerInteractEvent(player, new Vector(playerInteract.worldX, playerInteract.worldY));
        EventSubscriptions.call(event);
    }
}
