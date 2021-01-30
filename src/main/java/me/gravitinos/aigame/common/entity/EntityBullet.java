package me.gravitinos.aigame.common.entity;

import me.gravitinos.aigame.common.map.GameWorld;

public class EntityBullet extends GameEntity {

    int life = 50 * 10;

    public EntityBullet(GameWorld world) {
        super(world);
        setFrictionFactor(0D);
    }

    @Override
    protected void doTick() {
        if(life-- <= 0)
            this.remove();
    }
}
