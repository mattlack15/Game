package me.gravitinos.aigame.client.world;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.entity.EntityPlayer;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.map.Chunk;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.util.BlockVector;
import me.gravitinos.aigame.common.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ClientWorld extends GameWorld {

    private Map<UUID, GameEntity> entityList = new ConcurrentHashMap<>();

    private GameClient client;

    public ClientWorld(String name, GameClient client) {
        super(name);
        this.client = client;
    }

    public List<GameEntity> getEntities() {
        return new ArrayList<>(this.entityList.values());
    }

    public Collection<GameEntity> entityCollection() {
        return entityList.values();
    }

    public GameEntity getEntity(UUID id) {
        return this.entityList.get(id);
    }

    @Override
    public synchronized List<EntityPlayer> getPlayers() {
        return Collections.singletonList(client.player);
    }

    @Override
    public synchronized List<GameEntity> getEntitiesNear(Vector pos, double radius) {
        List<GameEntity> list = new ArrayList<>();
        double radiusSq = radius * radius;
        entityList.forEach((a, e) -> {
            if (e.getPosition().distanceSquared(pos) <= radiusSq)
                list.add(e);
        });
        return list;
    }

    @Override
    public synchronized void entityJoinWorld(GameEntity entity) {
        this.entityList.put(entity.getId(), entity);
    }

    @Override
    public synchronized void entityLeaveWorld(GameEntity entity) {
        this.entityList.remove(entity.getId());
    }

    @Override //Players are treated as entities
    public synchronized void playerJoinWorld(EntityPlayer player) {
        entityJoinWorld(player);
    }
    @Override
    public synchronized void playerLeaveWorld(EntityPlayer player) {
        entityLeaveWorld(player);
    }

    @Override
    public synchronized void entityUpdatePosition(GameEntity entity, Vector oldPos, Vector newPos) {
        //Do nothing, clients don't store entities in chunks
    }
}
