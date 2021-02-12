package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.packet.PacketPlayAudio;

public class PacketHandlerAudio implements PacketHandlerClient {
    @Override
    public void handlePacket(Packet packet, GameClient client) {
        PacketPlayAudio audioPacket = (PacketPlayAudio) packet;
        client.audioProvider.queueAudioPacket(audioPacket);
    }
}
