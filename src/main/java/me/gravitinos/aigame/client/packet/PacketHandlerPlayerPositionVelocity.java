package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.packet.PacketInPositionConfirmation;
import me.gravitinos.aigame.common.packet.PacketOutEntityPositionVelocity;

import java.util.UUID;

public class PacketHandlerPlayerPositionVelocity implements PacketHandlerClient {

    private int count = 0;
    private long ms = System.currentTimeMillis();

    @Override
    public void handlePacket(Packet pack, GameClient client) {
        PacketOutEntityPositionVelocity packet = (PacketOutEntityPositionVelocity) pack;
        UUID id = packet.entityId;
        GameEntity entity = client.player.getId().equals(id) ? client.player : client.world.getEntity(id);
        if(entity == null) {
            System.out.println("Could not find entity " + id.toString().substring(0, 3) + " from " + client.player.getId().toString().substring(0, 3));
            return;
        }
        if(entity == client.player) {
            client.player.getConnection().sendPacket(new PacketInPositionConfirmation(packet.pId));
        }
        entity.setPositionInternal(packet.position);
        entity.setVelocityInternal(packet.velocity);
    }
}
