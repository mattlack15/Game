package me.gravitinos.aigame.common.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SharedPalette<T> {
    private Map<Integer, T> palette = new ConcurrentHashMap<>();

    public void setPalette(Map<Integer, T> palette) {
        this.palette.clear();
        this.palette.putAll(palette);
    }

    public T get(int id) {
        return palette.get(id);
    }
}
