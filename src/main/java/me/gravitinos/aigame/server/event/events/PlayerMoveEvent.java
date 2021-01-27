package me.gravitinos.aigame.server.event.events;

import me.gravitinos.aigame.common.util.Vector;
import me.gravitinos.aigame.server.event.Cancellable;
import me.gravitinos.aigame.server.event.Event;
import me.gravitinos.aigame.server.player.ServerPlayer;

public class PlayerMoveEvent extends Event implements Cancellable {

    public ServerPlayer getPlayer() {
        return player;
    }

    private ServerPlayer player;
    private Vector newPosition;
    private Vector oldPosition;
    private boolean cancelled = false;

    public PlayerMoveEvent(ServerPlayer player, Vector oldPosition, Vector newPosition) {
        this.oldPosition = oldPosition;
        this.newPosition = newPosition;
        this.player = player;
    }

    public Vector getNewPosition() {
        return newPosition;
    }

    public void setNewPosition(Vector newPosition) {
        this.newPosition = newPosition;
    }

    public Vector getOldPosition() {
        return oldPosition;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
