package me.gravitinos.aigame.common.datawatcher;

import net.ultragrav.serializer.GravSerializer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataWatcher {
    private static class DataWatcherEntry<T> {
        T obj = null;
        boolean meta = true;
        boolean dirty = false;
    }

    private static Map<Object, AtomicInteger> idCounters = new ConcurrentHashMap<>();

    public static <T> DataWatcherObject register(Object identifier) {
        return register(identifier, true);
    }

    public static <T> DataWatcherObject register(Object identifier, boolean isMeta) {
        idCounters.putIfAbsent(identifier, new AtomicInteger());
        AtomicInteger counter = idCounters.get(identifier);
        DataWatcherObject object = new DataWatcherObject(counter.getAndIncrement());
        object.isMeta = isMeta;
        return object;
    }

    private Map<Integer, DataWatcherEntry<?>> entryMap = new HashMap<>();
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    public <T> void set(DataWatcherObject object, T value) {
        lock.writeLock().lock();
        try {
            int id = object.id;
            //Get entry
            entryMap.putIfAbsent(id, new DataWatcherEntry<>());
            DataWatcherEntry entry = entryMap.get(id);
            if(!Objects.equals(entry.obj, value)) {
                entry.obj = value;
                entry.dirty = true;
            }
            entry.meta = object.isMeta;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public <T> T get(DataWatcherObject object) {
        lock.readLock().lock();
        try {
            int id = object.id;
            if(!entryMap.containsKey(id)) {
                return null;
            }

            DataWatcherEntry entry = entryMap.get(id);
            return (T) entry.obj;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isDirty(DataWatcherObject object) {
        lock.readLock().lock();
        try {
            int id = object.id;
            if(!entryMap.containsKey(id)) {
                return false;
            }
            return entryMap.get(id).dirty;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean setDirty(DataWatcherObject object, boolean dirty) {
        lock.writeLock().lock();
        try {
            int id = object.id;
            if(!entryMap.containsKey(id)) {
                return false;
            }
            boolean wasDirty = entryMap.get(id).dirty;
            entryMap.get(id).dirty = dirty;
            return wasDirty;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean anyMetaDirty() {
        lock.readLock().lock();
        try {
            for (Map.Entry<Integer, DataWatcherEntry<?>> entry : this.entryMap.entrySet()) {
                if(entry.getValue().meta && entry.getValue().dirty) {
                    return true;
                }
            }
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void serializeDirtyMeta(GravSerializer serializer, boolean markNonDirty) {
        lock.writeLock().lock();
        try {
            serializer.writeInt(this.entryMap.size());
            this.entryMap.forEach((id, data) -> {
                if(data.meta) {
                    serializer.writeInt(id);
                    serializer.writeObject(data);
                }
            });
        } finally {
            lock.writeLock().unlock();
        }
    }
}
