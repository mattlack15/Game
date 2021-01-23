package me.gravitinos.aigame.server.player;

import lombok.Getter;
import me.gravitinos.aigame.common.connection.PlayerConnection;
import me.gravitinos.aigame.common.entity.EntityPlayer;
import me.gravitinos.aigame.common.inventory.Inventory;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.util.Vector;

import java.util.UUID;

public class ServerPlayer extends EntityPlayer {

    private int renderDistance = 4;

    private Inventory inventory = new Inventory(5);

    @Getter
    private PlayerChunkMap chunkMap = new PlayerChunkMap();

    public ServerPlayer(GameWorld world, UUID id, String name,  PlayerConnection connection) {
        super(world, connection);
        this.setId(id);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    protected void doTick() {
    }
}
