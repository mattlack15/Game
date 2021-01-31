package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.packet.PacketOutTitle;

import java.awt.*;

public class PacketHandlerTitle implements PacketHandlerClient {
    @Override
    public void handlePacket(Packet packet, GameClient client) {
        PacketOutTitle title = (PacketOutTitle) packet;
        client.title = title.message;
        client.titleTicksLeft = title.displayTicks;
        client.titleFadeInTicks = title.fadeInTicks;
        client.titleFadeOutTicks = title.fadeOutTicks;
        client.titleColour = new Color(240, 160, 0, 0).getRGB();
    }
}
