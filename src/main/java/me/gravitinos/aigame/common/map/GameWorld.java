package me.gravitinos.aigame.common.map;

import lombok.Getter;
import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.entity.EntityPlayer;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.util.BlockVector;
import me.gravitinos.aigame.common.util.Vector;

import java.util.*;

public class GameWorld {
    private Map<BlockVector, Chunk> loadedChunks = new HashMap<>();
    @Getter
    private String name;

    private Set<EntityPlayer> players = new HashSet<>();

    public GameWorld(String name) {
        this.name = name;
    }

    public synchronized List<Chunk> getLoadedChunks() {
        return new ArrayList<>(this.loadedChunks.values());
    }

    public synchronized Chunk createChunk(int cx, int cy) {
        Vector pos = new Vector(cx, cy);
        Chunk chunk = new Chunk(pos);
        initChunk(chunk);
        this.loadedChunks.put(new BlockVector(cx, cy), chunk);
        return chunk;
    }

    protected void initChunk(Chunk chunk) {
    }

    public synchronized Chunk getChunkAt(int cx, int cy) {
        Chunk chunk = loadedChunks.get(new BlockVector(cx, cy));
        if (chunk != null)
            return chunk;
        return createChunk(cx, cy);
    }

    public synchronized Chunk getLoadedChunkAt(int cx, int cy) {
        return getChunkAt0(cx, cy);
    }

    private synchronized Chunk getChunkAt0(int cx, int cy) {
        return loadedChunks.get(new BlockVector(cx, cy));
    }

    public void setBlockAt(double x, double y, GameBlock block) {
        setBlockAt((int) x, (int) y, block);
    }

    public void setBlockAt(int x, int y, GameBlock block) {
        int cx = x >> 4;
        int cy = y >> 4;
        Chunk chunk = getChunkAt(cx, cy);
        chunk.setBlock(x & 15, y & 15, block);
    }

    public synchronized void unloadChunkAt(int cx, int cz) {
        this.loadedChunks.remove(new BlockVector(cx, cz));
    }

    public void setBlockAt(Vector pos, GameBlock block) {
        setBlockAt(pos.getX(), pos.getY(), block);
    }

    public GameBlock getBlockAt(Vector blockPos) {
        return this.getBlockAt(blockPos.getX(), blockPos.getY());
    }

    public GameBlock getBlockAt(int x, int y) {
        int cx = x >> 4;
        int cy = y >> 4;
        Chunk chunk = getChunkAt(cx, cy);
        return chunk.getBlock(x & 15, y & 15);
    }

    public GameBlock getBlockAt(double x, double y) {
        x = Math.floor(x);
        y = Math.floor(y);
        return getBlockAt((int) x, (int) y);
    }

    public synchronized List<GameEntity> getEntitiesNear(Vector pos, double radius) {
        int xStart = (int) (pos.getX() - radius) >> 4;
        int xEnd = (int) (pos.ceil().getX() + radius) >> 4;
        int yEnd = (int) (pos.ceil().getY() + radius) >> 4;
        int yStart = (int) (pos.getY() - radius) >> 4;

        List<GameEntity> entities = new ArrayList<>();

        double radiusSquared = radius * radius;

        for (int x = xStart; x <= xEnd; x++) {
            for (int y = yStart; y <= yEnd; y++) {
                Chunk chunk = getChunkAt0(x, y);
                if (chunk == null)
                    continue;
                chunk.getEntityList(entities);
            }
        }

        return entities;
    }

    public synchronized List<GameEntity> getEntities() {
        List<GameEntity> collected = new ArrayList<>();
        loadedChunks.forEach((k, c) -> collected.addAll(c.getEntityList()));
        return collected;
    }

    public synchronized List<EntityPlayer> getPlayers() {
        return new ArrayList<>(players);
    }

    public synchronized void playerJoinWorld(EntityPlayer player) {
        this.players.add(player);
    }

    public synchronized void playerLeaveWorld(EntityPlayer player) {
        this.players.remove(player);
    }

    public synchronized void entityJoinWorld(GameEntity entity) {
        entity.getChunk(true).addEntity(entity);
    }

    public synchronized void entityLeaveWorld(GameEntity entity) {
        Chunk chunk = entity.getChunk();
        if (chunk != null)
            chunk.removeEntity(entity.getId());
    }

    public synchronized void entityUpdatePosition(GameEntity entity, Vector oldPos, Vector newPos) {

        //Maybe call an event here

        if (entity instanceof EntityPlayer) //Don't do any chunk stuff with players
            return;

        Chunk chunk;

        if (oldPos != null) {
            if ((int) oldPos.getX() >> 4 == (int) newPos.getX() >> 4 &&
                    (int) oldPos.getY() >> 4 == (int) newPos.getY() >> 4) {
                return;
            }

            chunk = getLoadedChunkAt((int) oldPos.getX() >> 4, (int) oldPos.getY() >> 4);
            if (chunk != null) {
                chunk.removeEntity(entity.getId());
            }
        }

        if (newPos != null) {
            chunk = getChunkAt((int) newPos.getX() >> 4, (int) newPos.getY() >> 4);
            if (!chunk.getEntityList().contains(entity))
                chunk.addEntity(entity);
        }
    }

    public synchronized EntityPlayer getPlayer(UUID id) {
        for (EntityPlayer players : getPlayers())
            if (players.getId().equals(id))
                return players;
        return null;
    }

    public synchronized EntityPlayer getPlayer(String name) {
        for (EntityPlayer player : getPlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

    public synchronized void tick() {
        List<EntityPlayer> players = getPlayers();
        Iterator<Map.Entry<BlockVector, Chunk>> it = this.loadedChunks.entrySet().iterator();
        OUTER:
        while (it.hasNext()) {
            Map.Entry<BlockVector, Chunk> entry = it.next();
            Chunk c = entry.getValue();
            for (EntityPlayer player : players) {
                if (player.getPosition().distanceSquared(c.getPosition().multiply(16)) < 200 * 200)
                    continue OUTER;
            }
            for (GameEntity entity : c.getEntityList()) {
                if (entity instanceof EntityPlayer)
                    continue OUTER;
            }
            it.remove();
        }
    }
}
