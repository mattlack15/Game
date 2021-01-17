package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.map.Chunk;
import me.gravitinos.aigame.common.packet.PacketOutMapChunk;

public class PacketHandlerMapChunk implements PacketHandler<PacketOutMapChunk> {

    @Override
    public void handlePacket(PacketOutMapChunk packet, GameClient client) {
        Chunk chunk = client.world.getChunkAt(packet.cx, packet.cy);
        short[] data = packet.data;
        for (int i = 0, dataLength = data.length; i < dataLength; i++) {
            short datum = data[i];
            chunk.setBlockIndex(i, datum);
        }
    }
}
