package me.gravitinos.aigame.common.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class GameBlock {

    private static Map<Integer, GameBlock> instances = new ConcurrentHashMap<>();
    private static Map<GameBlock, Integer> ids = new ConcurrentHashMap<>();
    private static Map<String, GameBlock> names = new ConcurrentHashMap<>();
    private static AtomicInteger idCounter = new AtomicInteger();

    public static int registerBlock(GameBlock block, String identifier) {
        int id = idCounter.getAndIncrement();
        instances.put(id, block);
        ids.put(block, id);
        names.put(identifier.toLowerCase(), block);
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

    public static Map<String, GameBlock> getBlockNameMap() {
        return new HashMap<>(names);
    }

    public static GameBlock getBlock(String name) {
        name = name.toLowerCase();
        return names.get(name);
    }

    public abstract boolean isSolid();
}
