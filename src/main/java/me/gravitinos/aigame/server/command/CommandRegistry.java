package me.gravitinos.aigame.server.command;

import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class CommandRegistry {
    public static final CommandRegistry DEFAULT_REGISTRY = new CommandRegistry();

    private Map<String, Command> registry = new ConcurrentHashMap<>();

    public Command get(String cmd) {
        return registry.get(cmd.toLowerCase());
    }

    public void register(String cmd, Command handler) {
        registry.put(cmd.toLowerCase(), handler);
    }

    public void unregister(Command handler) {
        for(Map.Entry<String, Command> entry : new HashMap<>(registry).entrySet()) {
            if(Objects.equals(entry.getValue(), handler)) {
                registry.remove(entry.getKey());
            }
        }
    }
}
