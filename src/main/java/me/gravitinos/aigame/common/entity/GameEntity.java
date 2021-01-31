package me.gravitinos.aigame.common.entity;

import lombok.Getter;
import lombok.Setter;

import me.gravitinos.aigame.common.datawatcher.DataWatcher;
import me.gravitinos.aigame.common.datawatcher.DataWatcherObject;
import me.gravitinos.aigame.common.map.Chunk;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.util.AxisAlignedBoundingBox;
import me.gravitinos.aigame.common.util.BlockVector;
import me.gravitinos.aigame.common.util.SharedPalette;
import me.gravitinos.aigame.common.util.Vector;
import net.ultragrav.serializer.GravSerializer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class GameEntity {

    public static final DataWatcherObject W_HEALTH = DataWatcher.register(GameEntity.class, false);
    public static final DataWatcherObject W_POSITION = DataWatcher.register(GameEntity.class, false);
    public static final DataWatcherObject W_LAST_POSITION = DataWatcher.register(GameEntity.class, false);
    public static final DataWatcherObject W_VELOCITY = DataWatcher.register(GameEntity.class, false);
    public static final DataWatcherObject W_FRICTION_FACTOR = DataWatcher.register(GameEntity.class, false);
    public static final DataWatcherObject W_DO_MOVEMENT_PREDICTION = DataWatcher.register(GameEntity.class); //For client

    @Getter
    private DataWatcher dataWatcher = new DataWatcher();

    @Setter
    private AxisAlignedBoundingBox hitbox = new AxisAlignedBoundingBox(0, 0);
    @Getter
    private Vector position = new Vector(0,0);
    @Getter
    private Vector velocity = new Vector(0, 0);
    @Getter
    private GameWorld world;
    @Getter
    private double frictionFactor = 1D;

    @Getter
    @Setter
    private UUID id;

    protected boolean dead = true;

    //Registry
    private static Map<String, Class<? extends GameEntity>> REGISTRY_NAME = new ConcurrentHashMap<>();
    private static Map<Class<? extends GameEntity>, String> REGISTRY_CLASS = new ConcurrentHashMap<>();

    /**
     * Register an entity with the game
     * @param name The formal name of the entity ex. "game.fire"
     * @param clazz The class of the entity
     */
    public static void registerEntity(String name, Class<? extends GameEntity> clazz) {
        REGISTRY_NAME.put(name, clazz);
        REGISTRY_CLASS.put(clazz, name);
    }

    public static List<String> getRegisteredEntities() {
        return new ArrayList<>(REGISTRY_NAME.keySet());
    }

    public static Class<? extends GameEntity> byName(String name) {
        return REGISTRY_NAME.get(name);
    }

    public static String byClass(Class<? extends GameEntity> clazz) {
        return REGISTRY_CLASS.get(clazz);
    }
    //

    public GameEntity(GameWorld world) {
        this(world, UUID.randomUUID());
    }

    public GameEntity(GameWorld world, UUID id) {
        this.id = id;
        this.world = world;
        this.setPositionInternal(new Vector(0, 0));
        this.setVelocityInternal(new Vector(0, 0));

        getDataWatcher().set(W_FRICTION_FACTOR, frictionFactor, 0);
        getDataWatcher().set(W_DO_MOVEMENT_PREDICTION, false, 0);
    }

    /**
     * Check for a collision at the position given
     */
    protected boolean checkCollision(Vector pos, double multiplier) {
        int searchX = (int) Math.abs(this.velocity.getX() * multiplier) + 2;
        int searchY = (int) Math.abs(this.velocity.getY() * multiplier) + 2;
        AxisAlignedBoundingBox ourBb = new AxisAlignedBoundingBox(getHitbox().getSizeX(), getHitbox().getSizeY());
        ourBb.updatePosition(pos);
        for (int x = -searchX; x < searchX; x++) {
            for (int y = -searchY; y < searchY; y++) {
                BlockVector v = new BlockVector((int) pos.getX() + x, (int) (pos.getY() + y));
                if (world.getBlockAt(v.getX(), v.getY()).isSolid()) {
                    AxisAlignedBoundingBox bb = new AxisAlignedBoundingBox(1, 1);
                    bb.updatePosition(new Vector(v.getX(), v.getY()));
                    if (ourBb.intersects(bb)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get whether the client should perform movement prediction for this entity
     */
    public boolean shouldDoMovementPrediction() {
        return getDataWatcher().get(W_DO_MOVEMENT_PREDICTION);
    }

    public void setShouldDoMovementPrediction(boolean val) {
        getDataWatcher().set(W_DO_MOVEMENT_PREDICTION, val);
    }

    /**
     * Updates position with velocity
     */
    public synchronized void tick1(double multiplier) {
        if (!this.velocity.isZero()) {
            Vector newPos = this.position.add(this.velocity.multiply(multiplier));
            this.setPosition(newPos);
        }
    }

    /**
     * Performs tick operations for the entity, ex. friction
     */
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

    /**
     * Called once per tick
     */
    protected abstract void doTick();


    public synchronized void setFrictionFactor(double frictionFactor) {
        this.frictionFactor = frictionFactor;
        this.dataWatcher.set(W_FRICTION_FACTOR, frictionFactor);
    }

    public synchronized AxisAlignedBoundingBox getHitbox() {
        this.hitbox.updatePosition(this.position);
        return this.hitbox;
    }

    /**
     * Set the velocity with a weak dirt level of 0
     */
    public synchronized void setVelocityInternal(Vector velocity) {
        this.velocity = velocity;
        this.dataWatcher.setState(W_VELOCITY, velocity, 0);
    }

    /**
     * Set the velocity with a weak dirt level of 1
     */
    public synchronized void setVelocity(Vector velocity) {
        this.velocity = velocity;
        this.dataWatcher.set(W_VELOCITY, velocity);
    }

    /**
     * Set the position with a weak dirt level of 0
     */
    public synchronized void setPositionInternal(Vector position) {
        setPosition(position, 0);
        getDataWatcher().set(W_LAST_POSITION, position, 0);
    }

    /**
     * Set the position with a weak dirt level of 2
     */
    public synchronized void setPosition(Vector position) {
        setPosition(position, 2);
    }

    /**
     * Set the position with the weak dirt level given
     */
    public synchronized void setPosition(Vector position, int weakDirt) {
        if (getPosition() != null && getPosition().equals(position))
            return;

        if (!dead)
            world.entityUpdatePosition(this, getPosition(), position);

        dataWatcher.set(W_POSITION, position, weakDirt);

        this.position = position;
    }

    /**
     * Adds this entity to the world it is associated with
     */
    public synchronized void joinWorld() {
        world.entityJoinWorld(this);
        dead = false;
    }

    public synchronized BlockVector getChunkLocation() {
        return new BlockVector((int) getPosition().getX() >> 4, (int) getPosition().getY() >> 4);
    }

    public synchronized Chunk getChunk() {
        return getChunk(false);
    }

    public synchronized Chunk getChunk(boolean load) {
        return load ? world.getChunkAt(getChunkLocation().getX(), getChunkLocation().getY()) :
                world.getLoadedChunkAt(getChunkLocation().getX(), getChunkLocation().getY());
    }

    /**
     * Remove this entity from the world it is associated with
     */
    public synchronized void remove() {
        world.entityLeaveWorld(this);
        dead = true;
    }

    /**
     * This is to be called when the data watcher's contents are changed so that this entity can update and internal variables with the new values
     */
    public void invalidateMeta() {
    }

    public static GameEntity deserialize(GravSerializer serializer, GameWorld world, SharedPalette<String> palette) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        int typeId = serializer.readInt();
        String type = palette.byId(typeId);
        Class<? extends GameEntity> clazz = REGISTRY_NAME.get(type);

        //Create the entity
        GameEntity entity = clazz.getConstructor(GameWorld.class).newInstance(world);

        //Set entity data values
        entity.setId(serializer.readUUID());
        entity.setPositionInternal(new Vector(serializer.readDouble(), serializer.readDouble()));
        entity.setVelocityInternal(new Vector(serializer.readDouble(), serializer.readDouble()));
        entity.frictionFactor = serializer.readDouble();
        entity.getDataWatcher().set(GameEntity.W_FRICTION_FACTOR, entity.frictionFactor, 0);

        //Set meta
        entity.getDataWatcher().updateStrongDirt(serializer, 0);
        entity.invalidateMeta();

        return entity;
    }

    public void serialize(GravSerializer serializer, SharedPalette<String> palette) {
        String type = GameEntity.byClass(this.getClass());
        if (type == null) {
            throw new IllegalStateException("Cannot serialize unregistered entity type: " + this.getClass().getName());
        }
        int entityTypeId = palette.getId(type);
        serializer.writeInt(entityTypeId);

        serializer.writeUUID(this.getId());

        serializer.writeDouble(this.getPosition().getX());
        serializer.writeDouble(this.getPosition().getY());

        serializer.writeDouble(this.getVelocity().getX());
        serializer.writeDouble(this.getVelocity().getY());

        serializer.writeDouble(this.getFrictionFactor());

        getDataWatcher().serializeMeta(serializer);
    }
}
