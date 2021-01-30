package me.gravitinos.aigame.common.blocks;

import me.gravitinos.aigame.common.util.SharedPalette;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class GameBlock {

    private static SharedPalette<GameBlock> ids = new SharedPalette<>();
    private static Map<String, GameBlock> names = new HashMap<>();
    private static AtomicInteger idCounter = new AtomicInteger();

    public static GameBlock registerBlock(GameBlock block, String identifier) {
        synchronized (GameBlock.class) {
            int id = idCounter.getAndIncrement();
            ids.put(block, id);
            names.put(identifier.toLowerCase(), block);
            return block;
        }
    }

    public static GameBlock getBlock(int id) {
        return ids.byId(id);
    }

    public static int getId(GameBlock block) {
        return ids.getId(block);
    }

    public static List<GameBlock> getBlocks() {
        synchronized (GameBlock.class) {
            return new ArrayList<>(names.values());
        }
    }

    public static Map<String, GameBlock> getBlockNameMap() {
        synchronized (GameBlock.class) {
            return new HashMap<>(names);
        }
    }

    public static GameBlock getBlock(String name) {
        name = name.toLowerCase();
        return names.get(name);
    }

    public abstract boolean isSolid();
}
