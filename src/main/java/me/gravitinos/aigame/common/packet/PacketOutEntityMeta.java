package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.entity.GameEntity;
import net.ultragrav.serializer.GravSerializer;

public class PacketOutEntityMeta extends Packet {

    public GravSerializer metadata;

    public PacketOutEntityMeta(GameEntity entity) {
        this.metadata = new GravSerializer();
        entity.getDataWatcher().serializeDirtyMeta(metadata, false);
    }

    public PacketOutEntityMeta(GravSerializer serializer) {
        this.metadata = serializer.readSerializer();
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeSerializer(this.metadata);
    }
}
