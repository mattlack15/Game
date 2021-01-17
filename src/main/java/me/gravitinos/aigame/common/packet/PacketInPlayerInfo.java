package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import net.ultragrav.serializer.GravSerializer;

import java.util.UUID;

/**
 * Player id, Player name
 */
public class PacketInPlayerInfo extends Packet {

    private UUID id;
    private String name;
    public PacketInPlayerInfo(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public PacketInPlayerInfo(GravSerializer serializer) {
        this.id = serializer.readUUID();
        this.name = serializer.readString();
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeUUID(id);
        serializer.writeString(name);
    }
}
