package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.packet.PacketOutSpawnEntity;
import net.ultragrav.serializer.GravSerializer;

import java.lang.reflect.InvocationTargetException;

public class PacketHandlerSpawnEntity implements PacketHandlerClient {
    @Override
    public void handlePacket(Packet packet, GameClient client) {
        PacketOutSpawnEntity p = (PacketOutSpawnEntity) packet;
        GravSerializer serializedEntity = p.serializedEntity;
        try {
            GameEntity entity = GameEntity.deserialize(serializedEntity, client.world, client.entityPalette);
            entity.joinWorld();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            System.out.println("Could not deserialize entity");
            e.printStackTrace();
        }

    }
}
