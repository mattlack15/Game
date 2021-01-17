package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.packet.PacketOutPlayerPositionVelocity;

public class PacketHandlerPlayerPositionVelocity implements PacketHandler<PacketOutPlayerPositionVelocity> {
    @Override
    public void handlePacket(PacketOutPlayerPositionVelocity packet, GameClient client) {
        client.player.setPositionInternal(packet.position);
        client.player.setVelocityInternal(packet.velocity);
    }
}
