package me.gravitinos.aigame.server.packet.handler;

import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.blocks.GameBlockType;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.map.Chunk;
import me.gravitinos.aigame.common.packet.PacketInPlayerMove;
import me.gravitinos.aigame.common.packet.PacketOutEntityPositionVelocity;
import me.gravitinos.aigame.common.packet.PacketOutMapChunk;
import me.gravitinos.aigame.common.util.Vector;
import me.gravitinos.aigame.server.GameServer;
import me.gravitinos.aigame.server.event.EventSubscriptions;
import me.gravitinos.aigame.server.event.events.PlayerMoveEvent;
import me.gravitinos.aigame.server.player.ServerPlayer;

public class PacketHandlerPlayerMove implements PacketHandlerServer {
    @Override
    public void handlePacket(ServerPlayer player, Packet packet, GameServer server) {
        PacketInPlayerMove move = (PacketInPlayerMove) packet;
        Vector movement = move.movement;

        if (player.isAwaitingTpConfirmation())
            return; //Ignore this packet

        Vector pos = player.getPosition().add(movement);

        PlayerMoveEvent event = new PlayerMoveEvent(player, player.getPosition(), pos);
        EventSubscriptions.call(event);
        boolean result = !event.isCancelled();

        player.setPosition(event.getNewPosition(), 1);
        if (!result) {
            player.setPosition(pos.subtract(movement), 2);
        } else {
            player.setVelocityInternal(event.getNewPosition().subtract(event.getOldPosition())); //TODO change to something more reliable
        }
    }
}
