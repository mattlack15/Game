package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.util.Vector;
import net.ultragrav.serializer.GravSerializer;

import java.util.UUID;

public class PacketOutEntityPositionVelocity extends Packet {

    public Vector position;
    public Vector velocity;
    public UUID entityId;
    public UUID pId = UUID.randomUUID();

    public PacketOutEntityPositionVelocity(UUID entityId, Vector position, Vector velocity) {
        this.entityId = entityId;
        this.position = position;
        this.velocity = velocity;
    }

    public PacketOutEntityPositionVelocity(GravSerializer serializer) {
        this.entityId = serializer.readUUID();
        this.pId = serializer.readUUID();
        this.position = new Vector(serializer.readDouble(), serializer.readDouble());
        this.velocity = new Vector(serializer.readDouble(), serializer.readDouble());
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeUUID(entityId);
        serializer.writeUUID(pId);
        serializer.writeDouble(position.getX());
        serializer.writeDouble(position.getY());
        serializer.writeDouble(velocity.getX());
        serializer.writeDouble(velocity.getY());
    }
}
