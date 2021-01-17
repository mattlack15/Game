package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.util.Vector;
import net.ultragrav.serializer.GravSerializer;

public class PacketInPlayerMove extends Packet {

    private Vector movement;

    public PacketInPlayerMove(Vector movement) {
        this.movement = movement;
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeDouble(movement.getX());
        serializer.writeDouble(movement.getY());
    }
}
