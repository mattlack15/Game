package me.gravitinos.aigame.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SharedPalette<T> {

    private T[] hashToObject; //Used for search
    private T[] idToObject; //Used for id to object
    private int[] hashToId; //Used for object to id

    private AtomicReference<Thread> writeThread = new AtomicReference<>();
    private AtomicInteger reading = new AtomicInteger();

    public SharedPalette(int size) {
        clear(size);
    }

    public SharedPalette() {
        this(4);
    }

    public void setPalette(Map<Integer, T> palette) {

        //Clear, resizing the array to be at least the size of the new palette
        clear(palette.size());
        palette.forEach((a, b) -> put(b, a));
    }

    /**
     * Clear and resize the palette
     */
    public void clear(int newSize) {
        attainWrite();
        try {
            if (newSize % 2 == 0)
                newSize++;
            hashToObject = (T[]) new Object[newSize];
            idToObject = (T[]) new Object[newSize];
            hashToId = new int[newSize];
        } finally {
            releaseWrite();
        }
    }

    public Map<Integer, T> getPalette() {
        attainRead();
        try {
            Map<Integer, T> map = new HashMap<>();
            for (int i = 0; i < idToObject.length; i++) {
                if (idToObject[i] != null) {
                    map.put(i, idToObject[i]);
                }
            }
            return map;
        } finally {
            releaseRead();
        }
    }

    /**
     * Put an object into the palette with an associated id
     */
    public void put(T obj, int id) {

        attainWrite();

        try {

            int size = hashToObject.length;
            while (size < id) {
                size <<= 1;
            }

            resize(size);

            int hash = getHash(obj);

            int freeIndex = search(hash, null);
            if (freeIndex == -1)
                throw new IllegalStateException("No more room D:");
            hashToObject[freeIndex] = obj;
            idToObject[id] = obj;
            hashToId[freeIndex] = id;

        } finally {
            releaseWrite();
        }
    }

    public T byId(int id) {
        return id >= idToObject.length ? null : idToObject[id];
    }

    public int getId(T obj) {
        attainRead();
        try {
            int hash = getHash(obj);
            if (hashToObject[hash] == obj)
                return hashToId[hash];
            return hashToId[search(hash, obj)];
        } finally {
            releaseRead();
        }
    }

    private void resize(int size) {

        attainWrite();

        try {

            if (hashToObject.length > size)
                return;

            T[] ITO = idToObject;

            hashToObject = (T[]) new Object[size];
            idToObject = (T[]) new Object[size];
            hashToId = new int[size];

            for (int i = 0; i < ITO.length; i++) {
                if (ITO[i] != null) {
                    put(ITO[i], i);
                }
            }
        } finally {
            releaseWrite();
        }
    }

    private int search(int start, T search) {
        attainRead();
        try {
            int i;
            for (i = start; i < hashToObject.length; i++) {
                if (hashToObject[i] == search) {
                    return i;
                }
            }

            for (i = 0; i < start; i++) {
                if (hashToObject[i] == search) {
                    return i;
                }
            }
            return -1;
        } finally {
            releaseRead();
        }
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

    private void attainRead() {
        reading.incrementAndGet();
        while(writeThread.get() != null) {
            Thread.yield();
        }
    }

    private void releaseRead() {
        reading.decrementAndGet();
    }

    private void attainWrite() {
        //Check if we already have it
        if(writeThread.get() == Thread.currentThread())
            return;

        //If we don't have it, try and get it, and if there are readers, yield
        while((writeThread.get() != Thread.currentThread() && !writeThread.compareAndSet(null, Thread.currentThread()))
        || reading.get() > 0)
            Thread.yield();
    }

    private void releaseWrite() {
        writeThread.compareAndSet(Thread.currentThread(), null);
    }
}
