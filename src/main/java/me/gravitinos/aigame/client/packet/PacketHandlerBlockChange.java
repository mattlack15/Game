package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.map.Chunk;
import me.gravitinos.aigame.common.packet.PacketOutBlockChange;

public class PacketHandlerBlockChange implements PacketHandlerClient {
    @Override
    public void handlePacket(Packet packet, GameClient client) {
        PacketOutBlockChange blockChange = (PacketOutBlockChange) packet;

        Chunk chunk = client.world.getLoadedChunkAt(blockChange.x >> 4, blockChange.y >> 4);
        if (chunk != null)
            chunk.setBlock(blockChange.x & 15, blockChange.y & 15, client.blockPalette.byId(((PacketOutBlockChange) packet).block));
    }
}
