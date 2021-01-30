package me.gravitinos.aigame.client;

import me.gravitinos.aigame.client.packet.*;
import me.gravitinos.aigame.client.render.block.BlockRender;
import me.gravitinos.aigame.client.render.block.BlockRenderAir;
import me.gravitinos.aigame.client.render.block.BlockRenderLightGreen;
import me.gravitinos.aigame.client.render.block.BlockRendererWall;
import me.gravitinos.aigame.client.render.entity.*;
import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.blocks.GameBlockType;
import me.gravitinos.aigame.common.entity.EntityBullet;
import me.gravitinos.aigame.common.entity.EntityFire;
import me.gravitinos.aigame.common.entity.EntityLine;
import me.gravitinos.aigame.common.entity.EntityPlayer;
import me.gravitinos.aigame.common.packet.*;

public class ClientRegistryInitializer {
    public static void init() {

        //Block renderers
        BlockRender.REGISTRY.put(GameBlockType.AIR, new BlockRenderAir());
        BlockRender.REGISTRY.put(GameBlockType.WALL, new BlockRendererWall());
        BlockRender.REGISTRY.put(GameBlockType.LIGHT_GREEN, new BlockRenderLightGreen());

        //Entity renderers
        EntityRender.REGISTRY.put(EntityPlayer.class, new EntityRenderPlayer());
        EntityRender.REGISTRY.put(EntityFire.class, new EntityRenderFire());
        EntityRender.REGISTRY.put(EntityLine.class, new EntityRenderLine());
        EntityRender.REGISTRY.put(EntityBullet.class, new EntityRenderBullet());

        //Packet Handlers
        PacketHandlerClient.REGISTRY.put(PacketOutMapChunk.class, new PacketHandlerMapChunk());
        PacketHandlerClient.REGISTRY.put(PacketOutEntityPositionVelocity.class, new PacketHandlerPlayerPositionVelocity());
        PacketHandlerClient.REGISTRY.put(PacketOutSpawnPlayer.class, new PacketHandlerSpawnPlayer());
        PacketHandlerClient.REGISTRY.put(PacketInOutChatMessage.class, new PacketHandlerIncChatMessage());
        PacketHandlerClient.REGISTRY.put(PacketOutSetPalette.class, new PacketHandlerSetPalette());
        PacketHandlerClient.REGISTRY.put(PacketOutSpawnEntity.class, new PacketHandlerSpawnEntity());
        PacketHandlerClient.REGISTRY.put(PacketOutDestroyEntity.class, new PacketHandlerDestroyEntity());
        PacketHandlerClient.REGISTRY.put(PacketInOutPing.class, new PacketHandlerPing());
    }
}
