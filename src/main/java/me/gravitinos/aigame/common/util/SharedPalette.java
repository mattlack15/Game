package me.gravitinos.aigame.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SharedPalette<T> {

    private T[] hashToObject; //Used for search
    private T[] idToObject; //Used for id to object
    private int[] hashToId; //Used for object to id

    public SharedPalette(int size) {
        clear(size);
    }

    public SharedPalette() {
        this(4);
    }

    public void setPalette(Map<Integer, T> palette) {
        clear(palette.size());
        palette.forEach((a, b) -> put(b, a));
    }

    public void clear(int newSize) {
        if(newSize % 2 == 0)
            newSize++;
        hashToObject = (T[]) new Object[newSize];
        idToObject = (T[]) new Object[newSize];
        hashToId = new int[newSize];
    }

    public synchronized Map<Integer, T> getPalette() {
        Map<Integer, T> map = new HashMap<>();
        for (int i = 0; i < idToObject.length; i++) {
            if(idToObject[i] != null) {
                map.put(i, idToObject[i]);
            }
        }
        return map;
    }

    public void put(T obj, int id) {

        int size = hashToObject.length;
        while(size < id) {
            size <<= 1;
        }

        resize(size);

        int hash = getHash(obj);

        int freeIndex = search(hash, null);
        if(freeIndex == -1)
            throw new IllegalStateException("No more room D:");
        hashToObject[freeIndex] = obj;
        idToObject[id] = obj;
        hashToId[freeIndex] = id;
    }

    public T byId(int id) {
        return id >= idToObject.length ? null : idToObject[id];
    }

    public int getId(T obj) {
        int hash = getHash(obj);
        if(hashToObject[hash] == obj)
            return hashToId[hash];
        return hashToId[search(hash, obj)];
    }

    private void resize(int size) {
        if(hashToObject.length > size)
            return;

        T[] ITO = idToObject;

        hashToObject = (T[]) new Object[size];
        idToObject = (T[]) new Object[size];
        hashToId = new int[size];

        for (int i = 0; i < ITO.length; i++) {
            if(ITO[i] != null) {
                put(ITO[i], i);
            }
        }
    }

    private int search(int start, T search) {
        int i;
        for (i = start; i < hashToObject.length; i++) {
            if(hashToObject[i] == search) {
                return i;
            }
        }

        for (i = 0; i < start; i++) {
            if(hashToObject[i] == search) {
                return i;
            }
        }
        return -1;
    }

    private int getHash(T obj) {
        return blurHash(System.identityHashCode(obj)) & (hashToId.length-1);
    }

    private int blurHash(int hash) {
        hash ^= hash >>> 16;
        hash *= -2048144789;
        hash ^= hash >>> 13;
        hash *= -1028477387;
        hash ^= hash >>> 16;
        return hash;
    }

}
