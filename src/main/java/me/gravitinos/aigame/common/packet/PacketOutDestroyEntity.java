package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import net.ultragrav.serializer.GravSerializer;

import java.util.UUID;

public class PacketOutDestroyEntity extends Packet {

    public UUID entityUUID;

    public PacketOutDestroyEntity(UUID id) {
        this.entityUUID = id;
    }

    public PacketOutDestroyEntity(GravSerializer serializer) {
        this.entityUUID = serializer.readUUID();
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeUUID(entityUUID);
    }
}
