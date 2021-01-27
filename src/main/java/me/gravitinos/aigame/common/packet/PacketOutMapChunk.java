package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.map.Chunk;
import net.ultragrav.serializer.GravSerializer;

public class PacketOutMapChunk extends Packet {
    public short[] data;
    public int cx;
    public int cy;

    public PacketOutMapChunk(Chunk chunk) {

        this.cx = (int) chunk.getPosition().getX();
        this.cy = (int) chunk.getPosition().getY();

        data = new short[256];
        chunk.exportBlocks(this.data);

    }

    public PacketOutMapChunk(GravSerializer serializer) {
        cx = serializer.readInt();
        cy = serializer.readInt();
        int size = serializer.readInt();
        data = new short[size];
        for (int i = 0; i < size; i++) {
            data[i] = serializer.readShort();
        }
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeInt(cx);
        serializer.writeInt(cy);
        serializer.writeInt(data.length);
        for (int i = 0; i < data.length; i++) {
            serializer.writeShort(data[i]);
        }
    }
}
