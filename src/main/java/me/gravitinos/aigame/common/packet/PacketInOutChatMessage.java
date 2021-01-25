package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import net.ultragrav.serializer.GravSerializer;

public class PacketInOutChatMessage extends Packet {

    public String message;

    public PacketInOutChatMessage(String message) {
        this.message = message;
    }

    public PacketInOutChatMessage(GravSerializer serializer) {
        message = serializer.readString();
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeString(message);
    }
}
