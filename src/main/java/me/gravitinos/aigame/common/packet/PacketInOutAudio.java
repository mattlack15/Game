package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import net.ultragrav.serializer.GravSerializer;

public class PacketInOutAudio extends Packet {
    public byte[] data;
    public int rate;
    public boolean compressed;

    public PacketInOutAudio(byte[] data, int rate, boolean compressed) {
        this.data = data;
        this.rate = rate;
        this.compressed = compressed;
    }

    public PacketInOutAudio(GravSerializer serializer) {
        this.data = serializer.readByteArray();
        this.rate = serializer.readInt();
        this.compressed = serializer.readBoolean();
    }


    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeByteArray(data);
        serializer.writeInt(rate);
        serializer.writeBoolean(compressed);
    }
}
