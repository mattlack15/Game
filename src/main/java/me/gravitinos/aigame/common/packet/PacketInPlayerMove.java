package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.util.Vector;
import net.ultragrav.serializer.GravSerializer;

public class PacketInPlayerMove extends Packet {

    public Vector movement;

    public PacketInPlayerMove(Vector movement) {
        this.movement = movement;
    }

    public PacketInPlayerMove(GravSerializer serializer) {
        this.movement = new Vector(serializer.readDouble(), serializer.readDouble());
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeDouble(movement.getX());
        serializer.writeDouble(movement.getY());
    }
}
