package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.util.Vector;
import net.ultragrav.serializer.GravSerializer;

import java.util.UUID;

public class PacketOutSpawnPlayer extends Packet {

    public UUID id;
    public String name;
    public Vector position;
    public Vector velocity;

    public PacketOutSpawnPlayer(UUID id, String name, Vector position, Vector velocity) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.velocity = velocity;
    }

    public PacketOutSpawnPlayer(GravSerializer serializer) {
        this.id = serializer.readUUID();
        this.name = serializer.readString();
        this.position = new Vector(serializer.readDouble(), serializer.readDouble());
        this.velocity = new Vector(serializer.readDouble(), serializer.readDouble());
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeUUID(id);
        serializer.writeString(name);
        serializer.writeDouble(this.position.getX());
        serializer.writeDouble(this.position.getY());
        serializer.writeDouble(this.velocity.getX());
        serializer.writeDouble(this.velocity.getY());
    }
}
