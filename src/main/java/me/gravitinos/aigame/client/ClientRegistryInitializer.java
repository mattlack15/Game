package me.gravitinos.aigame.client;

import me.gravitinos.aigame.client.packet.PacketHandler;
import me.gravitinos.aigame.client.packet.PacketHandlerMapChunk;
import me.gravitinos.aigame.client.packet.PacketHandlerPlayerPositionVelocity;
import me.gravitinos.aigame.client.render.block.BlockRender;
import me.gravitinos.aigame.client.render.block.BlockRenderAir;
import me.gravitinos.aigame.client.render.block.BlockRendererWall;
import me.gravitinos.aigame.client.render.entity.EntityRender;
import me.gravitinos.aigame.client.render.entity.EntityRenderFire;
import me.gravitinos.aigame.client.render.entity.EntityRenderPlayer;
import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.entity.EntityFire;
import me.gravitinos.aigame.common.entity.EntityPlayer;
import me.gravitinos.aigame.common.packet.PacketOutMapChunk;
import me.gravitinos.aigame.common.packet.PacketOutEntityPositionVelocity;

public class ClientRegistryInitializer {
    public static void init() {

        //Block renderers
        BlockRender.REGISTRY.put(GameBlock.getBlock(0), new BlockRenderAir());
        BlockRender.REGISTRY.put(GameBlock.getBlock(1), new BlockRendererWall());

        //Entity renderers
        EntityRender.REGISTRY.put(EntityPlayer.class, new EntityRenderPlayer());
        EntityRender.REGISTRY.put(EntityFire.class, new EntityRenderFire());

        //Packet Handlers
        PacketHandler.REGISTRY.put(PacketOutMapChunk.class, new PacketHandlerMapChunk());
        PacketHandler.REGISTRY.put(PacketOutEntityPositionVelocity.class, new PacketHandlerPlayerPositionVelocity());
    }
}
