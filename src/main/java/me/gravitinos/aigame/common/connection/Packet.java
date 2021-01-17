package me.gravitinos.aigame.common.connection;

import me.gravitinos.aigame.common.packet.PacketInPlayerInfo;
import me.gravitinos.aigame.common.packet.PacketInPlayerMove;
import net.ultragrav.serializer.GravSerializer;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Packet {

    private static Map<Integer, Class<? extends Packet>> idMap = new HashMap<>();
    private static Map<Class<? extends Packet>, Integer> packetMap = new HashMap<>();
    private static AtomicInteger idCounter = new AtomicInteger();

    static {
        registerPacket(PacketInPlayerInfo.class);
        registerPacket(PacketInPlayerMove.class);
    }

    static void registerPacket(Class<? extends Packet> packetClass) {
        int id = idCounter.getAndIncrement();
        idMap.put(id, packetClass);
        packetMap.put(packetClass, id);
    }

    public int getId() {
        Integer id = packetMap.get(this.getClass());
        return id == null ? -1 : id;
    }

    public abstract void serialize(GravSerializer serializer);

    public static <T extends Packet> T deserialize(GravSerializer serializer, int packetId) {
        Class<? extends Packet> c = idMap.get(packetId);
        try {
            Object o = c.getConstructor(GravSerializer.class).newInstance(serializer);
            return (T) o;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
