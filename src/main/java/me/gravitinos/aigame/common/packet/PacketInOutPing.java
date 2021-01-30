package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import net.ultragrav.serializer.GravSerializer;

public class PacketInOutPing extends Packet {

    public long ms1 = System.currentTimeMillis();
    public long ms2 = System.currentTimeMillis();

    public PacketInOutPing() {
    }

    public PacketInOutPing(GravSerializer serializer) {
        ms1 = serializer.readLong();
        ms2 = serializer.readLong();
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeLong(ms1);
        serializer.writeLong(ms2);
    }
}
