package me.gravitinos.aigame.common.map;

import lombok.Getter;
import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Chunk {
    private List<GameEntity> entityList = new ArrayList<>();
    private final AtomicIntegerArray blocks = new AtomicIntegerArray(new int[256]);
    @Getter
    private Vector position;

    public Chunk(Vector position) {
        this.position = position;
    }

    public boolean contains(Vector pos) {
        return (int) Math.floor(pos.getX()) >> 4 == position.getX() && (int) Math.floor(pos.getY()) >> 4 == position.getY();
    }

    public void setBlock(int bx, int by, GameBlock block) {
        int id = GameBlock.getId(block);
        blocks.set(bx << 4 | by, id);
    }

    public void exportBlocks(int[] array) {
        for (int i = 0; i < blocks.length(); i++) {
            array[i] = blocks.get(i);
        }
    }

    public void exportBlocks(short[] array) {
        for (int i = 0; i < blocks.length(); i++) {
            array[i] = (short) blocks.get(i);
        }
    }

    public void setBlockIndex(int index, int block) {
        blocks.set(index, block);
    }

    public void importBlocks(int[] array) {
        if (array.length > blocks.length())
            throw new IllegalStateException("Array must be <= chunk size (" + blocks.length() + ")");
        for (int i = 0; i < blocks.length(); i++) {
            blocks.set(i, array[i]);
        }
    }

    public GameBlock getBlock(int bx, int by) {
        return GameBlock.getBlock(blocks.get(bx << 4 | by));
    }

    public void setAll(GameBlock block) {
        int id = GameBlock.getId(block);
        for (int i = 0; i < 256; i++) {
            blocks.set(i, id);
        }
    }

    public synchronized List<GameEntity> getEntityList() {
        return new ArrayList<>(entityList);
    }

    public synchronized void getEntityList(List<GameEntity> list) {
        list.addAll(entityList);
    }

    public synchronized void removeEntity(UUID id) {
        this.entityList.removeIf(e -> e.getId().equals(id));
    }

    public synchronized void addEntity(GameEntity entity) {
        this.entityList.add(entity);
    }

}
