package me.gravitinos.aigame.common.datawatcher;

import me.gravitinos.aigame.common.util.Vector;
import net.ultragrav.serializer.GravSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataWatcher {
    private static class DataWatcherEntry<T> {
        T obj = null;
        boolean meta = true;
        int dirt = 0;
    }

    private static Map<Object, AtomicInteger> idCounters = new ConcurrentHashMap<>();

    public static <T> DataWatcherObject register(Class<?> identifier) {
        return register(identifier, true);
    }

    /**
     * Get a new data watcher object associated with the specified group (identifier)
     *
     * @param identifier The identifier of the group
     * @param isMeta     Whether the object should be considered meta
     * @return A new data watcher object that is unique within its group
     */
    public static <T> DataWatcherObject register(Class<?> identifier, boolean isMeta) {
        AtomicInteger counter = idCounters.get(identifier);
        Class<?> identifier0 = identifier;
        while (counter == null && identifier != Object.class) {
            identifier = identifier.getSuperclass();
            counter = idCounters.get(identifier);
        }
        if (counter == null) {
            counter = new AtomicInteger();
        }
        counter = new AtomicInteger(counter.get());
        idCounters.put(identifier0, counter);
        DataWatcherObject object = new DataWatcherObject(counter.getAndIncrement());
        object.isMeta = isMeta;
        return object;
    }

    private Map<Integer, DataWatcherEntry<?>> entryMap = new HashMap<>();
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Set the value of a field with a weak dirt level of 1
     */
    public <T> void set(DataWatcherObject object, T value) {
        set(object, value, 1);
    }


    /**
     * Set the value of a data object and if the object's dirt value is lower than the supplied dirt,
     * then the dirt value is updated to the supplied dirt.
     */
    public <T> void set(DataWatcherObject object, T value, int weakDirt) {
        lock.writeLock().lock();
        try {
            int id = object.id;
            //Get entry
            entryMap.putIfAbsent(id, new DataWatcherEntry<>());
            DataWatcherEntry entry = entryMap.get(id);
            if (!Objects.equals(entry.obj, value)) {
                entry.obj = value;
                entry.dirt = Math.max(entry.dirt, weakDirt);
            }
            entry.meta = object.isMeta;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Set the full state of a field
     */
    public <T> void setState(DataWatcherObject object, T value, int dirt) {
        lock.writeLock().lock();
        try {
            int id = object.id;
            //Get entry
            entryMap.putIfAbsent(id, new DataWatcherEntry<>());
            DataWatcherEntry entry = entryMap.get(id);
            if (!Objects.equals(entry.obj, value)) {
                entry.obj = value;
                entry.dirt = dirt;
            }
            entry.meta = object.isMeta;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Get the value of a field
     */
    public <T> T get(DataWatcherObject object) {
        lock.readLock().lock();
        try {
            int id = object.id;
            if (!entryMap.containsKey(id)) {
                return null;
            }

            DataWatcherEntry entry = entryMap.get(id);
            return (T) entry.obj;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Check if a field has at least the provided dirt level
     */
    public boolean isDirty(DataWatcherObject object, int requiredDirt) {
        lock.readLock().lock();
        try {
            int id = object.id;
            if (!entryMap.containsKey(id)) {
                return false;
            }
            return entryMap.get(id).dirt >= requiredDirt;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Set the dirt of a field
     */
    public int setDirt(DataWatcherObject object, int dirt) {
        lock.writeLock().lock();
        try {
            int id = object.id;
            if (!entryMap.containsKey(id)) {
                return 0;
            }
            int prevDirt = entryMap.get(id).dirt;
            entryMap.get(id).dirt = dirt;
            return prevDirt;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean anyMetaDirty() {
        lock.readLock().lock();
        try {
            for (Map.Entry<Integer, DataWatcherEntry<?>> entry : this.entryMap.entrySet()) {
                if (entry.getValue().meta && entry.getValue().dirt > 0) {
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
            AtomicInteger am = new AtomicInteger();
            this.entryMap.forEach((id, data) -> {
                if (data.meta && data.dirt > 0) {
                    am.getAndIncrement();
                }
            });
            serializer.writeInt(am.get());
            this.entryMap.forEach((id, data) -> {
                if (data.meta && data.dirt > 0) {
                    if (markNonDirty)
                        data.dirt = 0;
                    serializer.writeInt(id);
                    serializer.writeObject(data.obj);
                }
            });
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void serializeMeta(GravSerializer serializer) {
        lock.readLock().lock();
        try {
            AtomicInteger am = new AtomicInteger();
            this.entryMap.forEach((id, data) -> {
                if (data.meta) {
                    am.getAndIncrement();
                }
            });
            serializer.writeInt(am.get());
            this.entryMap.forEach((id, data) -> {
                if (data.meta) {
                    serializer.writeInt(id);
                    serializer.writeObject(data.obj);
                }
            });
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Update the fields provided in the serializer and set the dirt level to the provided dirt level
     */
    public void updateStrongDirt(GravSerializer serializer, int strongDirt) {
        lock.writeLock().lock();
        try {
            int amount = serializer.readInt();
            for (int i = 0; i < amount; i++) {
                int id = serializer.readInt();
                Object o = serializer.readObject();
                DataWatcherEntry entry = entryMap.get(id);
                if (entry != null) {
                    entry.obj = o;
                    entry.dirt = strongDirt;
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Update the fields provided in the serializer and set the dirt level to at least the provided dirt level
     */
    public void updateWeakDirt(GravSerializer serializer, int weakDirt) {
        lock.writeLock().lock();
        try {
            int amount = serializer.readInt();
            for (int i = 0; i < amount; i++) {
                int id = serializer.readInt();
                Object o = serializer.readObject();
                DataWatcherEntry entry = entryMap.get(id);
                if (entry != null) {
                    entry.obj = o;
                    entry.dirt = Math.max(entry.dirt, weakDirt);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
