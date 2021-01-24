package me.gravitinos.aigame.server;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.RegistryInitializer;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.connection.PlayerConnection;
import me.gravitinos.aigame.common.connection.SecuredTCPConnection;
import me.gravitinos.aigame.common.connection.SecuredTCPServer;
import me.gravitinos.aigame.common.datawatcher.PacketPackage;
import me.gravitinos.aigame.common.entity.EntityPlayer;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.packet.PacketInPlayerInfo;
import me.gravitinos.aigame.common.packet.PacketInPlayerMove;
import me.gravitinos.aigame.common.packet.PacketOutEntityPositionVelocity;
import me.gravitinos.aigame.common.packet.PacketOutSpawnPlayer;
import me.gravitinos.aigame.server.packet.handler.PacketHandlerPlayerMove;
import me.gravitinos.aigame.server.packet.handler.PacketHandlerServer;
import me.gravitinos.aigame.server.packet.provider.PacketProviderServerPlayer;
import me.gravitinos.aigame.server.player.ServerPlayer;
import me.gravitinos.aigame.server.world.ServerWorld;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

public class GameServer extends SecuredTCPServer {

    public GameWorld world;

    public GameServer(int port) {
        super(port);
    }

    @Override
    public void start() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException {
        super.start();

        System.out.println("Starting server on port " + this.getPort());

        initRegistries();

        //Create world
        world = new ServerWorld("The World");

        mainLoop();
    }

    private void initRegistries() {
        RegistryInitializer.init();
        PacketHandlerServer.REGISTRY.put(PacketInPlayerMove.class, new PacketHandlerPlayerMove());
    }

    private void mainLoop() {

        long lastTick;

        while (true) {

            lastTick = System.currentTimeMillis();
            tick();

            long wait = 50 - (System.currentTimeMillis() - lastTick);
            try {
                if (wait > 0)
                    Thread.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int tickCounter = 0;
    private long lastTickRecord = System.currentTimeMillis();
    private int tps = 0;

    public void tick() {

        tickCounter++;

        if(System.currentTimeMillis() - lastTickRecord >= 1000) {
            lastTickRecord = System.currentTimeMillis();
            tps = tickCounter;
            tickCounter = 0;
            System.out.println("TPS: " + tps);
        }

        //Tick entities
        this.world.getEntities().forEach(((e) -> {
            if (!(e instanceof EntityPlayer))
                e.tick1(1D);
            e.tick();
        }));

        //Tick world
        world.tick();

        //Send packets
        sendPackets();

        //Receive packets
        receivePackets();

    }

    private void receivePackets() {
        for (EntityPlayer player : world.getPlayers()) {
            while (player.getConnection().hasNextPacket()) {
                Packet packet = player.getConnection().nextPacket();
                PacketHandlerServer packetHandler = PacketHandlerServer.REGISTRY.get(packet.getClass());
                if (packetHandler == null) {
                    System.out.println("Could not handle packet: " + packet.getClass());
                    continue;
                }
                packetHandler.handlePacket((ServerPlayer) player, packet, this);
            }
        }
    }

    private void sendPackets() {
        for (EntityPlayer player : world.getPlayers()) {
            PacketPackage packets = new PacketProviderServerPlayer().getPackets((ServerPlayer) player, player.getDataWatcher());
            for (Packet packet : packets.self) {
                player.getConnection().sendPacket(packet);
            }
            for (Packet packet : packets.other) {
                for (EntityPlayer worldPlayer : world.getPlayers()) {
                    if (!worldPlayer.getId().equals(player.getId()))
                        worldPlayer.getConnection().sendPacket(packet);
                }
            }
        }
    }

    @Override
    public void handleConnection(SecuredTCPConnection connection) {
        try {
            PacketInPlayerInfo info = connection.nextPacket();
            UUID id = info.id;
            String name = info.name;
            PlayerConnection playerConnection = new PlayerConnection(connection);
            ServerPlayer player = new ServerPlayer(world, id, name, playerConnection);
            playerConnection.sendPacket(new PacketOutEntityPositionVelocity(id, player.getPosition(), player.getVelocity()));
            this.world.playerJoinWorld(player);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            try {
                new GameServer(6969).start();
            } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }, "Server Thread").start();

        Thread.sleep(10);
        new Thread(GameClient::new).start();

        Thread.sleep(3000);
        System.out.println("Next player joining...");
        new GameClient();
    }
}
