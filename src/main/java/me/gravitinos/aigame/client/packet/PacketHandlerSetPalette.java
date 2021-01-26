package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.packet.PacketOutSetPalette;

import java.util.HashMap;
import java.util.Map;

public class PacketHandlerSetPalette implements PacketHandlerClient {
    @Override
    public void handlePacket(Packet packet, GameClient client) {
        PacketOutSetPalette p = (PacketOutSetPalette) packet;
        Map<Integer, GameBlock> pal = new HashMap<>();
        p.blockPalette.forEach((id, name) -> pal.put(id, GameBlock.getBlock(name)));
        client.blockPalette.setPalette(pal);
        client.entityPalette.setPalette(p.entityPalette);
    }
}
