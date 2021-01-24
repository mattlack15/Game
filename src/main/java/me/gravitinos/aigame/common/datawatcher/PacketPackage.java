package me.gravitinos.aigame.common.datawatcher;

import me.gravitinos.aigame.common.connection.Packet;

import java.util.ArrayList;
import java.util.List;

public class PacketPackage {
    public final List<Packet> self = new ArrayList<>();
    public final List<Packet> other = new ArrayList<>();
}
