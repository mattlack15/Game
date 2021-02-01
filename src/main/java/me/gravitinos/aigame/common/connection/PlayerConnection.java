package me.gravitinos.aigame.common.connection;

import me.gravitinos.aigame.common.entity.EntityPlayer;

public class PlayerConnection {

    private SecuredTCPConnection connection;

    public PlayerConnection(SecuredTCPConnection connection) {
        this.connection = connection;
    }

    public synchronized void sendPacket(Packet packet) {
        try {
            connection.sendPacket(packet);
        } catch (Exception e) {
            this.close();
        }
    }

    public synchronized Packet nextPacket() {
        return connection.nextPacket();
    }

    public boolean hasNextPacket() {
        return connection.hasNext();
    }

    public void close() {
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isClosed() {
        return connection.isClosed();
    }
}
