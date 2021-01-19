package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.packet.PacketOutEntityPositionVelocity;

public class PacketHandlerPlayerPositionVelocity implements PacketHandler {
    @Override
    public void handlePacket(Packet pack, GameClient client) {
        PacketOutEntityPositionVelocity packet = (PacketOutEntityPositionVelocity) pack;
        GameEntity entity = client.world.getEntity(((PacketOutEntityPositionVelocity) pack).entityId);
        entity.setPositionInternal(packet.position);
        entity.setVelocityInternal(packet.velocity);
    }
}
