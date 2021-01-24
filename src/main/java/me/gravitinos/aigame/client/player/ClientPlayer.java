package me.gravitinos.aigame.client.player;

import me.gravitinos.aigame.client.PlayerCamera;
import me.gravitinos.aigame.client.Renderable;
import me.gravitinos.aigame.client.chat.ChatBox;
import me.gravitinos.aigame.common.connection.PlayerConnection;
import me.gravitinos.aigame.common.entity.EntityPlayer;
import me.gravitinos.aigame.common.inventory.Inventory;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.util.Vector;

import java.awt.*;
import java.util.UUID;

public class ClientPlayer extends EntityPlayer {

    private Inventory inventory = new Inventory(5);
    private ChatBox chatBox = new ChatBox(1000, 1000); //TODO fix

    public ClientPlayer(GameWorld world, UUID id, PlayerConnection connection) {
        super(world, id, connection);
    }

    @Override
    protected void doTick() {
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public ChatBox getChatBox() {
        return this.chatBox;
    }
}
