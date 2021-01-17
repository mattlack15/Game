package me.gravitinos.aigame.common.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class GameBlock {

    private static Map<Integer, GameBlock> instances = new ConcurrentHashMap<>();
    private static Map<GameBlock, Integer> ids = new ConcurrentHashMap<>();
    private static AtomicInteger idCounter = new AtomicInteger();

    public static int registerBlock(GameBlock block) {
        int id = idCounter.getAndIncrement();
        instances.put(id, block);
        ids.put(block, id);
        return id;
    }
    public static GameBlock getBlock(int id) {
        return instances.get(id);
    }
    public static int getId(GameBlock block) {
        return ids.get(block);
    }
    public static List<GameBlock> getBlocks() {
        return new ArrayList<>(instances.values());
    }

    public abstract boolean isSolid();
}
