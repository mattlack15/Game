package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import net.ultragrav.serializer.GravSerializer;

public class PacketOutTitle extends Packet {

    public String message;
    public int fadeInTicks = 0;
    public int displayTicks = 60;
    public int fadeOutTicks = 15;

    public PacketOutTitle(String message) {
        this.message = message;
    }

    public PacketOutTitle(String message, int fadeInTicks, int displayTicks, int fadeOutTicks) {
        this.message = message;
        this.fadeInTicks = fadeInTicks;
        this.displayTicks = displayTicks;
        this.fadeOutTicks = fadeOutTicks;
    }

    public PacketOutTitle(GravSerializer serializer) {
        this.message = serializer.readString();
        this.fadeInTicks = serializer.readInt();
        this.displayTicks = serializer.readInt();
        this.fadeOutTicks = serializer.readInt();
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeString(message);
        serializer.writeInt(fadeInTicks);
        serializer.writeInt(displayTicks);
        serializer.writeInt(fadeOutTicks);
    }
}
