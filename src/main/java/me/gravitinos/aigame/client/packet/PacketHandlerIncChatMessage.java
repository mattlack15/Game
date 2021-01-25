package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.packet.PacketInOutChatMessage;

public class PacketHandlerIncChatMessage implements PacketHandlerClient {
    @Override
    public void handlePacket(Packet packet, GameClient client) {
        client.player.getChatBox().chat(((PacketInOutChatMessage)packet).message);
    }
}
