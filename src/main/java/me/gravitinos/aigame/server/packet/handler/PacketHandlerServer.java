package me.gravitinos.aigame.server.packet.handler;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.server.GameServer;
import me.gravitinos.aigame.server.player.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public interface PacketHandlerServer {

    Map<Class<? extends Packet>, PacketHandlerServer> REGISTRY = new HashMap<>();

    void handlePacket(ServerPlayer player, Packet packet, GameServer server);
}
