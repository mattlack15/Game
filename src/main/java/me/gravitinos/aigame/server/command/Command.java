package me.gravitinos.aigame.server.command;

import me.gravitinos.aigame.server.player.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public abstract class Command {

    private String[] args = new String[0];
    private ServerPlayer player = null;
    private List<String> aliases = new ArrayList<>();

    public void addAlias(String alias) {
        aliases.add(alias);
    }

    public String getArg(int index) {
        return args[index];
    }

    public int getArgCount() {
        return args.length;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    public void handle(ServerPlayer player, String[] args) {
        init(player, args);
        execute();
    }

    private void init(ServerPlayer player, String args[]) {
        this.args = args;
        this.player = player;
    }

    protected void execute() {

    }

    public void register(CommandRegistry registry) {
        this.aliases.forEach(a -> registry.register(a, this));
    }
}
