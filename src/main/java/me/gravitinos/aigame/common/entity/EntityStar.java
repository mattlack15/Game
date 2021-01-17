package me.gravitinos.aigame.common.entity;

import me.gravitinos.aigame.client.PlayerCamera;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.util.Vector;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class EntityStar extends GameEntity {

    public int size = ThreadLocalRandom.current().nextInt(4) + 1;

    public EntityStar(GameWorld world) {
        super(world);
    }

    @Override
    protected void doTick() {

    }

}
