package me.gravitinos.aigame.server.event;

public interface Cancellable {
    boolean isCancelled();
    void setCancelled(boolean cancelled);
}
