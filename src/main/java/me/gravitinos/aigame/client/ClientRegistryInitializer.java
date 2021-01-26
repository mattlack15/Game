package me.gravitinos.aigame.client;

import me.gravitinos.aigame.client.packet.*;
import me.gravitinos.aigame.client.render.block.BlockRender;
import me.gravitinos.aigame.client.render.block.BlockRenderAir;
import me.gravitinos.aigame.client.render.block.BlockRendererWall;
import me.gravitinos.aigame.client.render.entity.EntityRender;
import me.gravitinos.aigame.client.render.entity.EntityRenderFire;
import me.gravitinos.aigame.client.render.entity.EntityRenderPlayer;
import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.entity.EntityFire;
import me.gravitinos.aigame.common.entity.EntityPlayer;
import me.gravitinos.aigame.common.packet.*;

public class ClientRegistryInitializer {
    public static void init() {

        //Block renderers
        BlockRender.REGISTRY.put(GameBlock.getBlock(0), new BlockRenderAir());
        BlockRender.REGISTRY.put(GameBlock.getBlock(1), new BlockRendererWall());

        //Entity renderers
        EntityRender.REGISTRY.put(EntityPlayer.class, new EntityRenderPlayer());
        EntityRender.REGISTRY.put(EntityFire.class, new EntityRenderFire());

        //Packet Handlers
        PacketHandlerClient.REGISTRY.put(PacketOutMapChunk.class, new PacketHandlerMapChunk());
        PacketHandlerClient.REGISTRY.put(PacketOutEntityPositionVelocity.class, new PacketHandlerPlayerPositionVelocity());
        PacketHandlerClient.REGISTRY.put(PacketOutSpawnPlayer.class, new PacketHandlerSpawnPlayer());
        PacketHandlerClient.REGISTRY.put(PacketInOutChatMessage.class, new PacketHandlerIncChatMessage());
        PacketHandlerClient.REGISTRY.put(PacketOutSetPalette.class, new PacketHandlerSetPalette());
    }
}
