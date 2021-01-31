package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.connection.Packet;

import java.util.HashMap;
import java.util.Map;

public interface PacketHandlerClient {

    Map<Class<? extends Packet>, PacketHandlerClient> REGISTRY = new HashMap<>();

    /**
     * Handle a packet
     * @param packet The packet
     * @param client The client
     */
    void handlePacket(Packet packet, GameClient client);
}
