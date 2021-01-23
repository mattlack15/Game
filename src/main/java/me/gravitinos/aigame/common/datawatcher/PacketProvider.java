package me.gravitinos.aigame.common.datawatcher;

import me.gravitinos.aigame.common.connection.Packet;

import java.util.List;

public abstract class PacketProvider<T> {
    public abstract List<Packet> getPacketsSelf(T obj, DataWatcher dataWatcher);
    public List<Packet> getPacketsOther(T obj, DataWatcher dataWatcher) {}
}
