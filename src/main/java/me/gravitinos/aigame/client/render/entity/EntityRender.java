package me.gravitinos.aigame.client.render.entity;

import me.gravitinos.aigame.client.render.block.BlockRender;
import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.util.Vector;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class EntityRender<T extends GameEntity> {
    public static final Map<Class<? extends GameEntity>, EntityRender<? extends GameEntity>> REGISTRY = new ConcurrentHashMap<>();

    public abstract void draw(Graphics graphics, T entity, Vector position, double scaleFactor);
}
