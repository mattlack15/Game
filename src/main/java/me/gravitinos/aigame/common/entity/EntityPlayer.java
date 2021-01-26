package me.gravitinos.aigame.common.entity;

import lombok.Getter;
import lombok.Setter;
import me.gravitinos.aigame.common.connection.PlayerConnection;
import me.gravitinos.aigame.common.datawatcher.DataWatcher;
import me.gravitinos.aigame.common.datawatcher.DataWatcherObject;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.inventory.Inventory;
import me.gravitinos.aigame.common.util.AxisAlignedBoundingBox;

import java.util.UUID;

public abstract class EntityPlayer extends GameEntity {

    public static final DataWatcherObject W_SIZE = DataWatcher.register(GameEntity.class);

    @Getter
    private PlayerConnection connection;

    @Getter
    @Setter
    private String name = "";

    public EntityPlayer(GameWorld world, UUID id, PlayerConnection connection) {
        super(world, id);
        this.setFrictionFactor(0.4D);
        this.getDataWatcher().set(W_SIZE, 0.8D, 0);
        this.setHitbox(new AxisAlignedBoundingBox(0.8D, 0.8D));
        this.connection = connection;
    }

    public double getSize() {
        return getDataWatcher().get(W_SIZE);
    }

    public void setSize(double size) {
        this.getDataWatcher().set(W_SIZE, size);
        this.setHitbox(new AxisAlignedBoundingBox(size, size));
    }

    @Override
    public synchronized void joinWorld() {
        getWorld().playerJoinWorld(this);
        this.dead = false;
    }

    @Override
    public synchronized void remove() {
        getWorld().playerLeaveWorld(this);
        this.dead = true;
    }

    public abstract Inventory getInventory();
}
