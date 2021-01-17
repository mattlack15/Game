package me.gravitinos.aigame.common.entity;

import me.gravitinos.aigame.client.PlayerCamera;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.util.Vector;

import java.awt.*;
import java.util.List;

public class EntityMagnet extends GameEntity {

    public double weight;
    public int strength;

    public EntityMagnet(GameWorld world, int strength, double weight) {
        super(world);
        this.strength = strength;
        this.weight = weight;
        this.setFrictionFactor(0);
    }

    @Override
    public void tick1(double multiplier) {
        super.tick1(multiplier);
        List<GameEntity> entityList = this.getWorld().getEntities();
        for(GameEntity entity : entityList) {
            if(!(entity instanceof EntityMagnet))
                continue;
            if(entity.equals(this))
                continue;
            Vector vel = getVelocity();
            Vector force = entity.getPosition().subtract(this.getPosition()).normalize()
                    .multiply(strength * 8D).multiply(((EntityMagnet) entity).strength * 8D)
                    .divide(Math.pow(getPosition().distance(entity.getPosition().multiply(8)), 2));
            if(this.weight > 20) {
                force.multiply(0);
            }
            if(entity.getPosition().distance(this.getPosition()) < 0.8D) {
                force = force.multiply(-0.1D);
            }
            this.setVelocity(vel.add(force.multiply(multiplier)));
        }
    }

    @Override
    protected void doTick() {
    }
}
