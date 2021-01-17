package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.util.Vector;
import net.ultragrav.serializer.GravSerializer;

public class PacketOutPlayerPositionVelocity extends Packet {

    public Vector position;
    public Vector velocity;

    public PacketOutPlayerPositionVelocity(Vector position, Vector velocity) {
        this.position = position;
        this.velocity = velocity;
    }

    public PacketOutPlayerPositionVelocity(GravSerializer serializer) {
        this.position = new Vector(serializer.readDouble(), serializer.readDouble());
        this.velocity = new Vector(serializer.readDouble(), serializer.readDouble());
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeDouble(position.getX());
        serializer.writeDouble(position.getY());
        serializer.writeDouble(velocity.getX());
        serializer.writeDouble(velocity.getY());
    }
}
