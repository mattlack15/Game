package me.gravitinos.aigame.common.map;

import lombok.Getter;
import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Chunk {
    private List<GameEntity> entityList = new ArrayList<>();
    private int[] blocks = new int[256];
    @Getter
    private Vector position;

    public Chunk(Vector position) {
        this.position = position;
    }

    public boolean contains(Vector pos) {
        return (int)Math.floor(pos.getX()) >> 4 == position.getX() && (int)Math.floor(pos.getY()) >> 4 == position.getY();
    }

    public synchronized void setBlock(int bx, int by, GameBlock block) {
        int id = GameBlock.getId(block);
        blocks[bx << 4 | by] = id;
    }

    public synchronized void exportBlocks(int[] array) {
        if(array.length < blocks.length)
            throw new IllegalStateException("Array length must be >= chunk size (" + blocks.length + ")");
        System.arraycopy(blocks, 0, array, 0, blocks.length);
    }

    public synchronized void exportBlocks(short[] array) {
        for (int i = 0; i < blocks.length; i++) {
            array[i] = (short) blocks[i];
        }
    }

    public synchronized void setBlockIndex(int index, int block) {
        blocks[index] = block;
    }

    public synchronized void importBlocks(int[] array) {
        if(array.length > blocks.length)
            throw new IllegalStateException("Array must be <= chunk size (" + blocks.length + ")");
        System.arraycopy(array, 0, blocks, 0, blocks.length);
    }

    public synchronized GameBlock getBlock(int bx, int by) {
        return GameBlock.getBlock(blocks[bx << 4 | by]);
    }

    public synchronized void setAll(GameBlock block) {
        int id = GameBlock.getId(block);
        for(int i = 0; i < 256; i++) {
            blocks[i] = id;
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
