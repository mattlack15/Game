package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.packet.PacketOutEntityPositionVelocity;

import java.util.UUID;

public class PacketHandlerPlayerPositionVelocity implements PacketHandlerClient {
    @Override
    public void handlePacket(Packet pack, GameClient client) {
        PacketOutEntityPositionVelocity packet = (PacketOutEntityPositionVelocity) pack;
        UUID id = packet.entityId;
        GameEntity entity = client.player.getId().equals(id) ? client.player : client.world.getEntity(id);
        entity.setPositionInternal(packet.position);
        //entity.setVelocityInternal(packet.velocity);
    }
}
