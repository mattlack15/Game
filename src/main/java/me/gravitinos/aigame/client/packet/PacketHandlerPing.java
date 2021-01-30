package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.connection.Packet;

public class PacketHandlerPing implements PacketHandlerClient {
    @Override
    public void handlePacket(Packet packet, GameClient client) {
        client.player.getConnection().sendPacket(packet);
    }
}
