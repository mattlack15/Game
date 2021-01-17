package me.gravitinos.aigame.common.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import me.gravitinos.aigame.client.Renderable;
import me.gravitinos.aigame.common.datawatcher.DataWatcher;
import me.gravitinos.aigame.common.datawatcher.DataWatcherObject;
import me.gravitinos.aigame.common.map.Chunk;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.util.AxisAlignedBoundingBox;
import me.gravitinos.aigame.common.util.Vector;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class GameEntity {

    public static final DataWatcherObject W_HEALTH = DataWatcher.register(GameEntity.class, false);
    public static final DataWatcherObject W_POSITION = DataWatcher.register(GameEntity.class, false);
    public static final DataWatcherObject W_LAST_POSITION = DataWatcher.register(GameEntity.class);
    public static final DataWatcherObject W_VELOCITY = DataWatcher.register(GameEntity.class, false);
    public static final DataWatcherObject W_FRICTION_FACTOR = DataWatcher.register(GameEntity.class, false);

    @Getter
    private DataWatcher dataWatcher = new DataWatcher();

    @Getter
    private AxisAlignedBoundingBox hitbox;
    @Getter
    private Vector position = null;
    @Getter
    private Vector velocity = new Vector(0, 0);
    @Getter
    private GameWorld world;
    @Getter
    private double frictionFactor = 1D;

    @Getter
    @Setter(value = AccessLevel.MODULE)
    private UUID id = UUID.randomUUID();

    public GameEntity(GameWorld world) {
        this.world = world;
        this.setPosition(new Vector(0, 0));
    }

    public synchronized void tick1(double multiplier) {
        if (!this.velocity.isZero()) {
            this.setPosition(position.add(this.velocity.multiply(multiplier)));
            Chunk currentChunk = world.getChunkAt((int) getPosition().floor().getX() >> 4, (int) getPosition().floor().getY() >> 4);
            if (!currentChunk.getEntityList().contains(this)) {
                System.out.println("Not in chunk");
            }
        }
    }

    public synchronized void tick() {
        if (frictionFactor != 0) {
            if (!this.velocity.isZero()) {
                this.velocity = this.velocity.multiply(1D - (0.05D * frictionFactor));
                if (this.velocity.getX() < 0.005D) {
                    this.velocity = this.velocity.setX(0D);
                }
                if (this.velocity.getY() < 0.005D) {
                    this.velocity = this.velocity.setY(0D);
                }
            }
        }
        doTick();
    }

    protected abstract void doTick();

    public synchronized void setFrictionFactor(double frictionFactor) {
        this.frictionFactor = frictionFactor;
        this.dataWatcher.set(W_FRICTION_FACTOR, frictionFactor);
    }

    public synchronized void setVelocityInternal(Vector velocity) {
        this.velocity = velocity;
        this.dataWatcher.setState(W_VELOCITY, velocity, false);
    }

    public synchronized void setVelocity(Vector velocity) {
        this.velocity = velocity;
        this.dataWatcher.set(W_VELOCITY, velocity);
    }

    public synchronized void setPositionInternal(Vector position) {
        setPosition(position, false);
    }

    public synchronized void setPosition(Vector position) {
        setPosition(position, true);
    }


    private synchronized void setPosition(Vector position, boolean record) {
        if (getPosition() != null && getPosition().equals(position))
            return;

        world.entityUpdatePosition(this, getPosition(), position);

        if (record) {
            dataWatcher.set(W_POSITION, position);
        } else {
            dataWatcher.setState(W_POSITION, position, false);
        }

        this.position = position;
    }

    public synchronized void remove() {
        Vector flooredPos = getPosition().floor();
        Chunk currentChunk = world.getChunkAt((int) flooredPos.getX() >> 4, (int) flooredPos.getY() >> 4);
        currentChunk.removeEntity(this.getId());
    }
}
