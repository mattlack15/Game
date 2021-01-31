package me.gravitinos.aigame;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.server.GameServer;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) {
  //      startServer();
        startClient();
    }

    public static void startServer() {
        new Thread(() -> {
            try {
                new GameServer(42070).start();
            } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException e) {
                e.printStackTrace();
            }
        }, "Server Thread").start();
    }

    public static void startClient() {
        new GameClient();
    }
}
