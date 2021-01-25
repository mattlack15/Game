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
    private double size = 0.8D;

    @Getter
    @Setter
    private String name = "";

    public EntityPlayer(GameWorld world, UUID id, PlayerConnection connection) {
        super(world, id);
        this.setFrictionFactor(0.45D);
        this.setHitbox(new AxisAlignedBoundingBox(size, size));
        this.connection = connection;
    }

    public void setSize(double size) {
        this.size = size;
        this.getDataWatcher().set(W_SIZE, size);
        this.setHitbox(new AxisAlignedBoundingBox(size, size));
    }

    public abstract Inventory getInventory();
}
