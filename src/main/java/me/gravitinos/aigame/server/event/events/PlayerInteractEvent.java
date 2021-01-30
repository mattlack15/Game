package me.gravitinos.aigame.server.event.events;

import me.gravitinos.aigame.common.util.Vector;
import me.gravitinos.aigame.server.event.Cancellable;
import me.gravitinos.aigame.server.event.Event;
import me.gravitinos.aigame.server.player.ServerPlayer;

public class PlayerInteractEvent extends Event implements Cancellable {

    public ServerPlayer getPlayer() {
        return player;
    }

    private ServerPlayer player;
    private Vector position;

    private boolean cancelled = false;


    public PlayerInteractEvent(ServerPlayer player, Vector position) {
        this.player = player;
        this.position = position;
    }

    public Vector getPosition() {
        return position;
    }

    public void setPosition(Vector position) {
        this.position = position;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
