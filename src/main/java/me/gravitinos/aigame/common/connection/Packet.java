package me.gravitinos.aigame.common.connection;

import me.gravitinos.aigame.common.packet.*;
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

        registerPacket(PacketInOutChatMessage.class);
        registerPacket(PacketInOutPing.class);
        registerPacket(PacketInOutAudio.class);

        registerPacket(PacketInPlayerInfo.class);
        registerPacket(PacketInPlayerMove.class);
        registerPacket(PacketInPositionConfirmation.class);
        registerPacket(PacketInDisconnect.class);
        registerPacket(PacketInPlayerInteract.class);

        registerPacket(PacketOutMapChunk.class);
        registerPacket(PacketOutEntityPositionVelocity.class);
        registerPacket(PacketOutSpawnPlayer.class);
        registerPacket(PacketOutSetPalette.class);
        registerPacket(PacketOutSpawnEntity.class);
        registerPacket(PacketOutDestroyEntity.class);
        registerPacket(PacketOutRemoteDisconnect.class);
        registerPacket(PacketOutTitle.class);
        registerPacket(PacketOutBlockChange.class);
    }

    static void registerPacket(Class<? extends Packet> packetClass) {
        int id = idCounter.getAndIncrement(); //TODO packet palette instead of this
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
        if(c == null) {
            System.out.println("Could not deserialize packet with id: " + packetId);
            return null;
        }
        try {
            Object o = c.getConstructor(GravSerializer.class).newInstance(serializer);
            return (T) o;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
