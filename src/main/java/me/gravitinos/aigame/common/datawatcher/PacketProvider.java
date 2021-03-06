package me.gravitinos.aigame.common.datawatcher;

import me.gravitinos.aigame.common.connection.Packet;

import java.util.List;

public abstract class PacketProvider<T> {
    public abstract PacketPackage getPackets(T obj, DataWatcher dataWatcher);
}
