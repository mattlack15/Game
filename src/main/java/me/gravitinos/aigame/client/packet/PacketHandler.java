package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.connection.Packet;

import java.util.HashMap;
import java.util.Map;

public interface PacketHandler {

    Map<Class<? extends Packet>, PacketHandler> REGISTRY = new HashMap<>();

    void handlePacket(Packet packet, GameClient client);
}
