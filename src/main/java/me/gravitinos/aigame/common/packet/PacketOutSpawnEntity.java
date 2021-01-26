package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.util.SharedPalette;
import net.ultragrav.serializer.GravSerializer;

import java.util.UUID;

public class PacketOutSpawnEntity extends Packet {

    public GameEntity entity;
    private SharedPalette<String> palette;

    public PacketOutSpawnEntity(GameEntity entity, SharedPalette<String> entityPalette) {
        this.entity = entity;
        this.palette = entityPalette;
    }

    @Override
    public void serialize(GravSerializer serializer) {
        entity.serialize(serializer, palette);
    }
}
