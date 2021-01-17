package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.client.player.ClientPlayer;
import me.gravitinos.aigame.common.connection.Packet;

import java.util.HashMap;
import java.util.Map;

public interface PacketHandler<T extends Packet> {

    Map<Class<? extends Packet>, PacketHandler<? extends Packet>> REGISTRY = new HashMap<>();

    void handlePacket(T packet, GameClient client);
}
