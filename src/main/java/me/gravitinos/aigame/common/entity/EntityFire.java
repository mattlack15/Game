package me.gravitinos.aigame.common.entity;

import me.gravitinos.aigame.client.PlayerCamera;
import me.gravitinos.aigame.common.datawatcher.DataWatcher;
import me.gravitinos.aigame.common.datawatcher.DataWatcherObject;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.util.Vector;

import java.awt.*;
import java.util.Random;

public class EntityFire extends GameEntity {

    private long life;
    public int color;
    public int size;
    private int grad;

    private static Random random = new Random(System.currentTimeMillis());

    public static DataWatcherObject W_COLOR = DataWatcher.register(EntityFire.class);
    public static DataWatcherObject W_SIZE = DataWatcher.register(EntityFire.class);

    public EntityFire(GameWorld world) {
        super(world);

        this.setShouldDoMovementPrediction(true);

        this.setFrictionFactor(0D);

        this.life = random.nextInt(14) + 8;

        this.size = random.nextInt(6) + 6;
        this.getDataWatcher().set(W_SIZE, size, 0);

        int r = random.nextInt(10) + 0xF5 - 2;
        int g = random.nextInt(20) + 0x44 - 10;
        int b = 28;

        this.color = (0xCC << 24) | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
        this.getDataWatcher().set(W_COLOR, color, 0);

        int gradR = (int) ((0x00 - r) / this.life);
        int gradG = (int) ((0x00 - g) / this.life);
        int gradB = (int) ((0x00 - b) / this.life);
        this.grad = (gradR & 255) << 16 | (gradG & 255) << 8 | (gradB & 255);


    }


    @Override
    protected void doTick() {
        if(random.nextDouble() < 0.25D) {
            size--;
            this.getDataWatcher().set(W_SIZE, size);
        }

        this.color += grad;
        this.getDataWatcher().set(W_COLOR, color);
        if(--life == 0)
            this.remove();
    }

    @Override
    public void invalidateMeta() {
        this.color = getDataWatcher().get(W_COLOR);
        this.size = getDataWatcher().get(W_SIZE);
    }
}
