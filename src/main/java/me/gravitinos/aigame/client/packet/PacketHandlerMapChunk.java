package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.map.Chunk;
import me.gravitinos.aigame.common.packet.PacketOutMapChunk;

public class PacketHandlerMapChunk implements PacketHandlerClient {

    @Override
    public void handlePacket(Packet pack, GameClient client) {
        PacketOutMapChunk packet = (PacketOutMapChunk) pack;
        Chunk chunk = client.world.getChunkAt(packet.cx, packet.cy);
        short[] data = packet.data;
        for (int i = 0, dataLength = data.length; i < dataLength; i++) {
            short datum = data[i];
            chunk.setBlockIndex(i, GameBlock.getId(client.blockPalette.byId(datum)));
        }
    }
}
