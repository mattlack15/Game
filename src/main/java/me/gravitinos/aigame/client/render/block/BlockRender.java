package me.gravitinos.aigame.client.render.block;

import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.util.BlockVector;
import me.gravitinos.aigame.common.util.Vector;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BlockRender {
    public static final Map<GameBlock, BlockRender> REGISTRY = new ConcurrentHashMap<>();

    public abstract void draw(Graphics graphics, BlockVector position, double scaleFactor);
}
