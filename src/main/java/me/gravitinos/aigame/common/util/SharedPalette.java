package me.gravitinos.aigame.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SharedPalette<T> {
    private Map<Integer, T> palette = new ConcurrentHashMap<>();
    private Map<T, Integer> paletteId = new ConcurrentHashMap<>();

    public void setPalette(Map<Integer, T> palette) {
        this.palette.clear();
        this.palette.putAll(palette);
        this.palette.forEach((a, b) -> this.paletteId.put(b, a));
    }

    public T byId(int id) {
        return palette.get(id);
    }

    public int getId(T obj) {
        return this.paletteId.get(obj);
    }

    public Map<Integer, T> getPalette() {
        return new HashMap<>(palette);
    }
}
