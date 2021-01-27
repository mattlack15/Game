package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.client.player.ClientPlayer;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.packet.PacketOutSpawnPlayer;

public class PacketHandlerSpawnPlayer implements PacketHandlerClient {
    @Override
    public void handlePacket(Packet packet, GameClient client) {
        ClientPlayer player = new ClientPlayer(client.world, ((PacketOutSpawnPlayer)packet).id,null); //TODO: If making async-safe, look over this
        player.setName(((PacketOutSpawnPlayer) packet).name); //Maybe make the constructor take the name and id ^
        player.setPositionInternal(((PacketOutSpawnPlayer) packet).position);
        player.setVelocityInternal(((PacketOutSpawnPlayer) packet).velocity);
        player.joinWorld();
    }
}
