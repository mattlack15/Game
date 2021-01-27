package me.gravitinos.aigame.server.player;

import lombok.Getter;
import me.gravitinos.aigame.common.connection.PlayerConnection;
import me.gravitinos.aigame.common.entity.EntityPlayer;
import me.gravitinos.aigame.common.inventory.Inventory;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.packet.PacketInOutChatMessage;
import me.gravitinos.aigame.common.packet.PacketOutRemoteDisconnect;
import me.gravitinos.aigame.common.util.Vector;
import me.gravitinos.aigame.server.GameServer;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ServerPlayer extends EntityPlayer {

    private int renderDistance = 4;

    private Inventory inventory = new Inventory(5);

    @Getter
    private PlayerChunkMap chunkMap = new PlayerChunkMap();

    private UUID tpConfirmation = null;

    public ServerPlayer(GameWorld world, UUID id, String name,  PlayerConnection connection) {
        super(world, id, connection);
        this.setName(name);
    }

    public synchronized boolean isAwaitingTpConfirmation() {
        return tpConfirmation != null;
    }

    public synchronized void confirmTp(UUID tpId) {
        if(tpConfirmation != null && tpConfirmation.equals(tpId))
            tpConfirmation = null;
    }

    public synchronized void setTpConfirmation(UUID tpId) {
        tpConfirmation = tpId;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void sendMessage(String message) {
        PacketInOutChatMessage packet = new PacketInOutChatMessage(message);
        getConnection().sendPacket(packet);
    }

    public void kick(String message, GameServer server) {
        if(message == null)
            message = "You were kicked from the server.";
        getConnection().sendPacket(new PacketOutRemoteDisconnect(message));
        server.handleDisconnect(this);
    }

    @Override
    protected void doTick() {
    }
}
