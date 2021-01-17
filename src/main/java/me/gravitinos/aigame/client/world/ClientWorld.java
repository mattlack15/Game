package me.gravitinos.aigame.client.world;

import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.map.Chunk;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.util.BlockVector;
import me.gravitinos.aigame.common.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ClientWorld extends GameWorld {

    private List<GameEntity> entityList = Collections.synchronizedList(new ArrayList<>());

    public ClientWorld(String name) {
        super(name);
    }

    public List<GameEntity> getEntities() {
        return this.entityList;
    }

    @Override
    public synchronized List<GameEntity> getEntitiesNear(Vector pos, double radius) {
        List<GameEntity> list = new ArrayList<>();
        double radiusSq = radius * radius;
        entityList.forEach((e) -> {
            if(e.getPosition().distanceSquared(pos) <= radiusSq)
                list.add(e);
        });
        return list;
    }

    @Override
    public synchronized void entityUpdatePosition(GameEntity entity, Vector oldPos, Vector newPos) {
        if(oldPos == null) {
            //Just joining the world
            //Add the entity to the world
            this.entityList.add(entity);
        }
    }
}
