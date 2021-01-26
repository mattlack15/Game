package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import net.ultragrav.serializer.GravSerializer;

import java.util.Map;

public class PacketOutSetPalette extends Packet {
    public Map<Integer, String> blockPalette;
    public Map<Integer, String> entityPalette;

    public PacketOutSetPalette(Map<Integer, String> blockPalette, Map<Integer, String> entityPalette) {
        this.blockPalette = blockPalette;
        this.entityPalette = entityPalette;
    }

    public PacketOutSetPalette(GravSerializer serializer) {
        this.blockPalette = serializer.readObject();
        this.entityPalette = serializer.readObject();
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeObject(blockPalette);
        serializer.writeObject(entityPalette);
    }
}
