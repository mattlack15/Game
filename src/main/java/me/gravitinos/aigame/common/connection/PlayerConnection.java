package me.gravitinos.aigame.common.connection;

import me.gravitinos.aigame.common.entity.EntityPlayer;

public class PlayerConnection {

    private SecuredTCPConnection connection;

    public PlayerConnection(SecuredTCPConnection connection) {
        this.connection = connection;
    }

    public synchronized void sendPacket(Packet packet) {
        connection.sendPacket(packet);
    }

    public synchronized Packet nextPacket() {
        return connection.nextPacket();
    }

    public boolean hasNextPacket() {
        return connection.hasNext();
    }
}
