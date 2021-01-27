package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import net.ultragrav.serializer.GravSerializer;

public class PacketOutRemoteDisconnect extends Packet {

    public String reason;

    public PacketOutRemoteDisconnect(String reason) {
        this.reason = reason;
    }

    public PacketOutRemoteDisconnect(GravSerializer serializer) {
        this.reason = serializer.readString();
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeString(reason);
    }
}
