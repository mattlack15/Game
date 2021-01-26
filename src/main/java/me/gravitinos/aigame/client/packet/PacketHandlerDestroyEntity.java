package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.packet.PacketOutDestroyEntity;

import java.util.UUID;

public class PacketHandlerDestroyEntity implements PacketHandlerClient {
    @Override
    public void handlePacket(Packet packet, GameClient client) {
        PacketOutDestroyEntity p = (PacketOutDestroyEntity) packet;
        UUID id = p.entityUUID;
        GameEntity entity = client.world.getEntity(id);
        if (entity != null)
            entity.remove();
    }
}
