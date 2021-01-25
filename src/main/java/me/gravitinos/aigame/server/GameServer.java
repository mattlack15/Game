package me.gravitinos.aigame.server;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.RegistryInitializer;
import me.gravitinos.aigame.common.blocks.GameBlockType;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.connection.PlayerConnection;
import me.gravitinos.aigame.common.connection.SecuredTCPConnection;
import me.gravitinos.aigame.common.connection.SecuredTCPServer;
import me.gravitinos.aigame.common.datawatcher.PacketPackage;
import me.gravitinos.aigame.common.entity.EntityPlayer;
import me.gravitinos.aigame.common.map.Chunk;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.packet.*;
import me.gravitinos.aigame.common.util.Vector;
import me.gravitinos.aigame.server.packet.handler.PacketHandlerChatMessage;
import me.gravitinos.aigame.server.packet.handler.PacketHandlerPlayerMove;
import me.gravitinos.aigame.server.packet.handler.PacketHandlerServer;
import me.gravitinos.aigame.server.packet.provider.PacketProviderServerPlayer;
import me.gravitinos.aigame.server.player.ServerPlayer;
import me.gravitinos.aigame.server.world.ServerWorld;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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
        PacketHandlerServer.REGISTRY.put(PacketInOutChatMessage.class, new PacketHandlerChatMessage());
    }

    private void mainLoop() {

        long lastTick;

        while (true) {

            lastTick = System.currentTimeMillis();
            tick();

            long wait = 33 - (System.currentTimeMillis() - lastTick);
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

        if (System.currentTimeMillis() - lastTickRecord >= 1000) {
            lastTickRecord = System.currentTimeMillis();
            tps = tickCounter;
            tickCounter = 0;
            System.out.println("TPS: " + tps);
        }

        long tickTiming = System.currentTimeMillis();

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

        tickTiming = System.currentTimeMillis() - tickTiming;

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

    public String onChatMessage(ServerPlayer player, String message) {
        if (message.startsWith("/")) {
            message = message.substring(1);
            String[] elements = message.split(" ");
            String[] args = new String[Math.max(elements.length - 1, 0)];
            for (int i = 1; i < elements.length; i++) {
                args[i - 1] = elements[i];
            }
            onCommand(player, elements.length > 0 ? elements[0] : "", args);
            return null;
        } else {
            return player.getName() + " > " + message;
        }
    }

    private Map<UUID, Vector> pos1s = new HashMap<>();

    public void onCommand(ServerPlayer player, String cmd, String[] args) {
        if (cmd.equalsIgnoreCase("ping")) {
            player.sendMessage("Server : " + "Pong!");
        } else if (cmd.equalsIgnoreCase("tp")) {
            if (args.length < 1) {
                player.sendMessage("Usage: /tp <player>");
                return;
            }
            ServerPlayer to = null;
            for (EntityPlayer worldPlayer : world.getPlayers()) {
                if (worldPlayer.getName().equalsIgnoreCase(args[0])) {
                    to = (ServerPlayer) worldPlayer;
                    break;
                }
            }

            if (to == player) {
                player.sendMessage("... That's you...");
                return;
            }

            if (to == null) {
                player.sendMessage("Player not found!");
                return;
            }

            player.sendMessage("Teleporting to " + to.getName());
            player.setPosition(to.getPosition());
        } else if (cmd.equalsIgnoreCase("pos1")) {
            pos1s.put(player.getId(), player.getPosition());
            player.sendMessage("Pos 1 set!");
        } else if (cmd.equalsIgnoreCase("set")) {
            if (pos1s.get(player.getId()) == null) {
                player.sendMessage("You need to set a pos1 with /pos1 first.");
                return;
            }
            Vector pos1 = pos1s.get(player.getId());
            List<Chunk> toUpdate = new ArrayList<>();
            Vector pos2 = player.getPosition();

            Vector sPos1 = pos1;
            pos1 = new Vector(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()));
            pos2 = new Vector(Math.max(sPos1.getX(), pos2.getX()), Math.max(sPos1.getY(), pos2.getY()));

            Chunk lastChunk = null;
            for (int x = (int) pos1.getX(); x < pos2.getX(); x++) {
                for (int y = (int) pos1.getY(); y < pos2.getY(); y++) {
                    Chunk chunk = player.getWorld().getChunkAt(x >> 4, y >> 4);
                    if (chunk != lastChunk) {
                        if (!toUpdate.contains(chunk))
                            toUpdate.add(chunk);
                        lastChunk = chunk;
                    }
                    player.getWorld().setBlockAt(x, y, GameBlockType.WALL);
                }
            }
            toUpdate.forEach(c -> {
                PacketOutMapChunk packetOutMapChunk = new PacketOutMapChunk(c);
                player.getWorld().getPlayers().forEach(p -> p.getConnection().sendPacket(packetOutMapChunk));
            });
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
