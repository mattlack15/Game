package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import net.ultragrav.serializer.GravSerializer;

public class PacketInPlayerInteract extends Packet {

    public double worldX;
    public double worldY;

    public PacketInPlayerInteract(double worldX, double worldY) {
        this.worldX = worldX;
        this.worldY = worldY;
    }

    public PacketInPlayerInteract(GravSerializer serializer) {
        this.worldX = serializer.readDouble();
        this.worldY = serializer.readDouble();
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeDouble(worldX);
        serializer.writeDouble(worldY);
    }
}
