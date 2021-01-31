package me.gravitinos.aigame.common.datawatcher;

/**
 * Object used for identifying unique fields within a data watcher
 */
public class DataWatcherObject {
    final int id;
    boolean isMeta = true;
    DataWatcherObject(int id) {
        this.id = id;
    }
}
