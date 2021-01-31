package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import net.ultragrav.serializer.GravSerializer;

public class PacketOutBlockChange extends Packet {

    public int x;
    public int y;
    public int block;

    public PacketOutBlockChange(int x, int y, int block) {
        this.x = x;
        this.y = y;
        this.block = block;
    }

    public PacketOutBlockChange(GravSerializer serializer) {
        this.x = serializer.readInt();
        this.y = serializer.readInt();
        this.block = serializer.readInt();
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeInt(x);
        serializer.writeInt(y);
        serializer.writeInt(block);
    }
}
