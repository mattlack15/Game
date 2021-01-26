package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.util.SharedPalette;
import net.ultragrav.serializer.GravSerializer;

public class PacketOutSpawnEntity extends Packet {

    public GravSerializer serializedEntity = new GravSerializer();

    public PacketOutSpawnEntity(GameEntity entity, SharedPalette<String> entityPalette) {
        entity.serialize(serializedEntity, entityPalette);
    }

    public PacketOutSpawnEntity(GravSerializer serializer) {
        this.serializedEntity = serializer.readSerializer();
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeSerializer(this.serializedEntity);
    }
}