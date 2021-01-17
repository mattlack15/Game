package me.gravitinos.aigame.client.render.block;

import me.gravitinos.aigame.common.blocks.GameBlock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlockRenderRegistryInitializer {
    public static void init() {
        BlockRender.REGISTRY.put(GameBlock.getBlock(0), new BlockRenderAir());
    }
}
