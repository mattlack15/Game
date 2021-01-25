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
import me.gravitinos.aigame.common.util.BlockVector;
import me.gravitinos.aigame.common.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class GameEntity {

    public static final DataWatcherObject W_HEALTH = DataWatcher.register(GameEntity.class, false);
    public static final DataWatcherObject W_POSITION = DataWatcher.register(GameEntity.class, false);
    public static final DataWatcherObject W_LAST_POSITION = DataWatcher.register(GameEntity.class);
    public static final DataWatcherObject W_VELOCITY = DataWatcher.register(GameEntity.class, false);
    public static final DataWatcherObject W_FRICTION_FACTOR = DataWatcher.register(GameEntity.class, false);

    @Getter
    private DataWatcher dataWatcher = new DataWatcher();

    @Setter
    private AxisAlignedBoundingBox hitbox = new AxisAlignedBoundingBox(0, 0);
    @Getter
    private Vector position = null;
    @Getter
    private Vector velocity = new Vector(0, 0);
    @Getter
    private GameWorld world;
    @Getter
    private double frictionFactor = 1D;

    @Getter
    @Setter
    private UUID id = UUID.randomUUID();

    //Registry
    private static Map<String, Class<? extends GameEntity>> REGISTRY = new ConcurrentHashMap<>();

    public static void registerEntity(String name, Class<? extends GameEntity> clazz) {
        REGISTRY.put(name, clazz);
    }

    public static Class<? extends GameEntity> byName(String name) {
        return REGISTRY.get(name);
    }
    //

    public GameEntity(GameWorld world) {
        this.world = world;
        this.setPositionInternal(new Vector(0, 0));
        this.setVelocityInternal(new Vector(0, 0));
    }

    public GameEntity(GameWorld world, UUID id) {
        this.id = id;
        this.world = world;
        this.setPositionInternal(new Vector(0, 0));
        this.setVelocityInternal(new Vector(0, 0));
    }

    private boolean checkCollision(Vector pos, double multiplier) {
        int searchX = (int) Math.abs(this.velocity.getX() * multiplier) + 2;
        int searchY = (int) Math.abs(this.velocity.getY() * multiplier) + 2;
        AxisAlignedBoundingBox ourBb = new AxisAlignedBoundingBox(getHitbox().getSizeX(), getHitbox().getSizeY());
        ourBb.updatePosition(pos);
        for (int x = -searchX; x < searchX; x++) {
            for (int y = -searchY; y < searchY; y++) {
                BlockVector v = new BlockVector((int) pos.getX() + x, (int) (pos.getY() + y));
                if(world.getBlockAt(v.getX(), v.getY()).isSolid()) {
                    AxisAlignedBoundingBox bb = new AxisAlignedBoundingBox(1, 1);
                    bb.updatePosition(new Vector(v.getX(), v.getY()));
                    if(ourBb.intersects(bb)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public synchronized void tick1(double multiplier) {
        if (!this.velocity.isZero()) {
            Vector newPos = this.position.add(this.velocity.multiply(multiplier));

            Vector testPos = new Vector(newPos.getX(), this.position.getY());

            if(checkCollision(testPos, multiplier)) {
                testPos = testPos.setX(this.position.getX());
                setVelocityInternal(this.velocity.setX(0));
            }

            testPos = testPos.setY(newPos.getY());
            if(checkCollision(testPos, multiplier)) {
                testPos = testPos.setY(this.position.getY());
                setVelocityInternal(this.velocity.setY(0));
            }

            if(this.velocity.isZero())
                return;

            //System.out.println("Setting position to " + testPos);

            this.setPosition(testPos);
        }
    }

    public synchronized void tick() {
        if (frictionFactor != 0) {
            if (!this.velocity.isZero()) {
                this.velocity = this.velocity.multiply(1D - (0.05D * frictionFactor));
                if (this.velocity.getX() < 0.005D && this.velocity.getX() > -0.005D) {
                    this.velocity = this.velocity.setX(0D);
                }
                if (this.velocity.getY() < 0.005D && this.velocity.getY() > -0.005D) {
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

    public synchronized AxisAlignedBoundingBox getHitbox() {
        this.hitbox.updatePosition(this.position);
        return this.hitbox;
    }

    public synchronized void setVelocityInternal(Vector velocity) {
        this.velocity = velocity;
        this.dataWatcher.setState(W_VELOCITY, velocity, 0);
    }

    public synchronized void setVelocity(Vector velocity) {
        this.velocity = velocity;
        this.dataWatcher.set(W_VELOCITY, velocity);
    }

    public synchronized void setPositionInternal(Vector position) {
        setPosition(position, 0);
        getDataWatcher().set(W_LAST_POSITION, position, 0);
    }

    public synchronized void setPosition(Vector position) {
        setPosition(position, 2);
    }


    public synchronized void setPosition(Vector position, int weakDirt) {
        if (getPosition() != null && getPosition().equals(position))
            return;

        world.entityUpdatePosition(this, getPosition(), position);

        dataWatcher.set(W_POSITION, position, weakDirt);

        this.position = position;
    }

    public synchronized BlockVector getChunkLocation() {
        return new BlockVector((int) getPosition().getX() >> 4, (int) getPosition().getY() >> 4);
    }

    public synchronized Chunk getChunk() {
        return world.getLoadedChunkAt(getChunkLocation().getX(), getChunkLocation().getY());
    }

    public synchronized void remove() {
        Vector flooredPos = getPosition().floor();
        Chunk currentChunk = world.getChunkAt((int) flooredPos.getX() >> 4, (int) flooredPos.getY() >> 4);
        currentChunk.removeEntity(this.getId());
        world.entityUpdatePosition(this, getPosition(), null);
    }
}
