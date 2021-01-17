package me.gravitinos.aigame.client;

import me.gravitinos.aigame.client.packet.PacketHandler;
import me.gravitinos.aigame.client.packet.PacketHandlerMapChunk;
import me.gravitinos.aigame.client.packet.PacketHandlerPlayerPositionVelocity;
import me.gravitinos.aigame.client.render.block.BlockRender;
import me.gravitinos.aigame.client.render.block.BlockRenderAir;
import me.gravitinos.aigame.client.render.block.BlockRendererWall;
import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.packet.PacketOutMapChunk;
import me.gravitinos.aigame.common.packet.PacketOutPlayerPositionVelocity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientRegistryInitializer {
    public static void init() {

        //Block renderers
        BlockRender.REGISTRY.put(GameBlock.getBlock(0), new BlockRenderAir());
        BlockRender.REGISTRY.put(GameBlock.getBlock(1), new BlockRendererWall());

        //Packet Handlers
        PacketHandler.REGISTRY.put(PacketOutMapChunk.class, new PacketHandlerMapChunk());
        PacketHandler.REGISTRY.put(PacketOutPlayerPositionVelocity.class, new PacketHandlerPlayerPositionVelocity());
    }
}
