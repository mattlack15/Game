package me.gravitinos.aigame.common.packet;

import me.gravitinos.aigame.common.connection.Packet;
import net.ultragrav.serializer.GravSerializer;

import java.util.UUID;

public class PacketInPositionConfirmation extends Packet {
    public UUID pId;

    public PacketInPositionConfirmation(UUID pId) {
        this.pId = pId;
    }

    public PacketInPositionConfirmation(GravSerializer serializer) {
        this.pId = serializer.readUUID();
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeUUID(pId);
    }
}
