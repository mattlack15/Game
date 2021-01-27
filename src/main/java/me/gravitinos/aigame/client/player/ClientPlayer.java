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
    private ChatBox chatBox = new ChatBox();

    public ClientPlayer(GameWorld world, UUID id, PlayerConnection connection) {
        super(world, id, connection);
    }

    @Override
    protected void doTick() {
    }

    public boolean checkCollisions = false;

    @Override
    public synchronized void tick1(double multiplier) {
        if (!this.getVelocity().isZero()) {
            Vector newPos = this.getPosition().add(this.getVelocity().multiply(multiplier));

            Vector testPos = new Vector(newPos.getX(), this.getPosition().getY());

            if(checkCollisions && !checkCollision(this.getPosition(), multiplier)) {
                if (checkCollision(testPos, multiplier)) {
                    testPos = testPos.setX(this.getPosition().getX());
                    setVelocityInternal(this.getVelocity().setX(0));
                }

                testPos = testPos.setY(newPos.getY());
                if (checkCollision(testPos, multiplier)) {
                    testPos = testPos.setY(this.getPosition().getY());
                    setVelocityInternal(this.getVelocity().setY(0));
                }
            } else {
                testPos = newPos;
            }

            if (this.getVelocity().isZero())
                return;

            //System.out.println("Setting position to " + testPos);

            this.setPosition(testPos);
        }
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public ChatBox getChatBox() {
        return this.chatBox;
    }
}
