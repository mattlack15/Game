package me.gravitinos.aigame.server;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.connection.SecuredTCPConnection;
import me.gravitinos.aigame.common.connection.SecuredTCPServer;
import me.gravitinos.aigame.common.datawatcher.PacketProvider;
import me.gravitinos.aigame.common.entity.EntityPlayer;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.map.GameWorld;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class GameServer extends SecuredTCPServer {

    private GameWorld world;

    public GameServer(int port) {
        super(port);
    }

    @Override
    public void start() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException {
        super.start();

        //Create world
        world = new GameWorld("The World");

        mainLoop();
    }

    private void mainLoop() {

        long lastTick;

        while (true) {

            lastTick = System.currentTimeMillis();
            tick();

            long wait = 50 - (System.currentTimeMillis() - lastTick);
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void tick() {

        //Tick entities
        this.world.getEntities().forEach(((e) -> {
            if (!(e instanceof EntityPlayer))
                e.tick1(1D);
            e.tick();
        }));

        //Tick world
        world.tick();

        //Get packets to send

    }

    @Override
    public void handleConnection(SecuredTCPConnection connection) {
    }
}
