package me.gravitinos.aigame.server;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.RegistryInitializer;
import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.blocks.GameBlockType;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.connection.PlayerConnection;
import me.gravitinos.aigame.common.connection.SecuredTCPConnection;
import me.gravitinos.aigame.common.connection.SecuredTCPServer;
import me.gravitinos.aigame.common.datawatcher.PacketPackage;
import me.gravitinos.aigame.common.entity.*;
import me.gravitinos.aigame.common.map.Chunk;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.packet.*;
import me.gravitinos.aigame.common.util.SharedPalette;
import me.gravitinos.aigame.common.util.Vector;
import me.gravitinos.aigame.server.event.EventSubscriptions;
import me.gravitinos.aigame.server.event.EventSubscription;
import me.gravitinos.aigame.server.event.events.PlayerInteractEvent;
import me.gravitinos.aigame.server.event.events.PlayerMoveEvent;
import me.gravitinos.aigame.server.minigame.MazeGenerator;
import me.gravitinos.aigame.server.packet.handler.*;
import me.gravitinos.aigame.server.packet.provider.PacketProviderServerPlayer;
import me.gravitinos.aigame.server.player.ServerPlayer;
import me.gravitinos.aigame.server.world.ServerWorld;

import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;

public class GameServer extends SecuredTCPServer {

    public GameWorld world;
    public SharedPalette<String> entityPalette = new SharedPalette<>();

    public GameServer(int port) {
        super(port);
    }

    @Override
    public void start() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException {
        super.start();

        System.out.println("Starting server on " + this.getPort());

        initRegistries();

        //Create entity palette
        Map<Integer, String> entityPaletteMap = new HashMap<>();
        int id = 0;
        for (String registeredEntity : GameEntity.getRegisteredEntities()) {
            entityPaletteMap.put(id++, registeredEntity);
        }
        entityPalette.setPalette(entityPaletteMap);

        //Create world
        world = new ServerWorld("The World", entityPalette);

        mainLoop();
        System.exit(0);
    }

    private void initRegistries() {
        RegistryInitializer.init();
        PacketHandlerServer.REGISTRY.put(PacketInPlayerMove.class, new PacketHandlerPlayerMove());
        PacketHandlerServer.REGISTRY.put(PacketInOutChatMessage.class, new PacketHandlerChatMessage());
        PacketHandlerServer.REGISTRY.put(PacketInPositionConfirmation.class, new PacketHandlerPositionConfirmation());
        PacketHandlerServer.REGISTRY.put(PacketInDisconnect.class, new PacketHandlerDisconnect());
        PacketHandlerServer.REGISTRY.put(PacketInOutPing.class, new PacketHandlerPing());
        PacketHandlerServer.REGISTRY.put(PacketInPlayerInteract.class, new PacketHandlerPlayerInteract());
    }

    private AtomicBoolean stopping = new AtomicBoolean(false);

    private void mainLoop() {

        EventSubscriptions.subscribe(this, GameServer.class);

        long lastTick;

        System.out.println("Press ENTER to stop the server.");

        while (true) {

            lastTick = System.currentTimeMillis();
            tick();

            long wait = (1000 / 50) - (System.currentTimeMillis() - lastTick);
            try {
                if (wait > 0)
                    Thread.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                if (System.in.available() > 0) {
                    stop();
                    break;
                }
                if (stopping.get()) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (stopping.compareAndSet(false, true)) {
            System.out.println("Stopping server.");

            //TODO send kick packets
            world.getPlayers().forEach(p -> ((ServerPlayer) p).kick("Stopping server.", this));

            //TODO save chunks

        }
    }

    private int tickCounter = 0;
    private long lastTickRecord = System.currentTimeMillis();
    private int tps = 0;

    public void tick() {

        tickCounter++;

        //Receive packets
        receivePackets();

        if (System.currentTimeMillis() - lastTickRecord >= 1000) {
            lastTickRecord = System.currentTimeMillis();
            tps = tickCounter;
            tickCounter = 0;
            //System.out.println("TPS: " + tps);
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

        tickMaze();

        tickTiming = System.currentTimeMillis() - tickTiming;
    }

    private void receivePackets() {
        for (EntityPlayer player : world.getPlayers()) {
            try {
                while (!player.getConnection().isClosed() && player.getConnection().hasNextPacket()) {
                    Packet packet = player.getConnection().nextPacket();
                    if (packet == null)
                        continue;
                    PacketHandlerServer packetHandler = PacketHandlerServer.REGISTRY.get(packet.getClass());
                    if (packetHandler == null) {
                        System.out.println("Could not handle packet: " + packet.getClass());
                        continue;
                    }
                    packetHandler.handlePacket((ServerPlayer) player, packet, this);
                }
            } catch (Exception e) {
                try {
                    ((ServerPlayer) player).kick("Error with packet handling", this);
                } catch (Exception e1) {
                    handleDisconnect((ServerPlayer) player);
                }
                System.out.println(player.getName() + " was disconnected.");
            }
        }
    }

    private void sendPackets() {
        for (EntityPlayer player : world.getPlayers()) {
            PacketPackage packets = new PacketProviderServerPlayer().getPackets((ServerPlayer) player, player.getDataWatcher());
            try {
                for (Packet packet : packets.self) {
                    player.getConnection().sendPacket(packet);
                }
            } catch (Exception e) {
                handleDisconnect((ServerPlayer) player);
                break;
            }
            for (Packet packet : packets.other) {
                for (EntityPlayer worldPlayer : world.getPlayers()) {
                    try {
                        if (!worldPlayer.getId().equals(player.getId())) {
                            if (worldPlayer.getConnection().isClosed())
                                throw new IllegalStateException();
                            worldPlayer.getConnection().sendPacket(packet);
                        }
                    } catch (Exception e) {
                        handleDisconnect((ServerPlayer) worldPlayer);
                    }
                }
            }
        }
        ((ServerWorld)world).getAndClearUpdated().forEach((c) -> {
            PacketOutMapChunk chunkPacket = new PacketOutMapChunk(c);
            world.getPlayers().forEach(p -> {
                if (((ServerPlayer) p).getChunkMap().isLoaded((int) c.getPosition().getX(), (int) c.getPosition().getY()))
                    p.getConnection().sendPacket(chunkPacket);
            });
        });
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

        System.out.println(player.getName() + " entered command: /" + cmd + " " + String.join(" ", args));

        if (cmd.equalsIgnoreCase("help")) {
            player.sendMessage("You can do /help,");
            player.sendMessage("/ping");
            player.sendMessage("/tp <player>");
            player.sendMessage("/maze");
            player.sendMessage("/kick <player>");
        } else if (cmd.equalsIgnoreCase("ping")) {
            player.getConnection().sendPacket(new PacketInOutPing());
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
            for (int x = (int) pos1.getX(); x <= (int) pos2.getX(); x++) {
                for (int y = (int) pos1.getY(); y <= (int) pos2.getY(); y++) {
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

            if (pos2.distanceSquared(player.getPosition()) < pos1.distanceSquared(player.getPosition())) {
                player.setPosition(pos2.add(1, 1));
            } else {
                player.setPosition(pos1.add(-1, -1));
            }
        } else if (cmd.equalsIgnoreCase("maze")) {
            long ms = System.currentTimeMillis();
            int[][] maze = MazeGenerator.generate(41);
            long genMs = System.currentTimeMillis() - ms;
            MazeGenerator.placeMaze(maze, this, -60, -60);
            for (EntityPlayer worldPlayer : world.getPlayers()) {
                worldPlayer.setPosition(new Vector(-58.9, -58.9));
            }
            mazeEndPoints.clear();
            mazeCounter = 0;
            mazeEndPoints.add(new Vector(-59, -59));
            ms = System.currentTimeMillis() - ms;
            player.sendMessage("Created a 41x41 maze in " + ms + "ms. (gen " + genMs + "ms)");
        } else if (cmd.equalsIgnoreCase("kick")) {
            if (args.length < 1) {
                player.sendMessage("Player name needed!");
                return;
            }
            String name = args[0];
            ServerPlayer player1 = (ServerPlayer) world.getPlayer(name);
            if (player1 == null) {
                player.sendMessage("Player not found.");
                return;
            }
            player.sendMessage("You kicked " + player1.getName() + " from the server.");
            player1.kick("You were kicked from the server by " + player.getName() + ".", this);
        }
    }

    public void handleDisconnect(ServerPlayer player) {
        if (world.getPlayer(player.getId()) != null) {
            player.remove();
            world.getPlayers().forEach(p -> ((ServerPlayer) p).sendMessage(player.getName() + " left the server."));
            System.out.println("DISCONNECT: " + player.getName() + " disconnected.");
        }
        if (!player.getConnection().isClosed()) {
            player.getConnection().close();
        }
    }

    @Override
    public void handleConnection(SecuredTCPConnection connection) {
        try {
            PacketInPlayerInfo info = connection.nextPacket();
            UUID id = info.id;
            String name = info.name;

            int i = 2; //Start at name2 then name3 then name4 etc
            while (world.getPlayer(name) != null) {
                name = info.name + (i++);
            }

            AtomicReference<ServerPlayer> playerAtomicReference = new AtomicReference<>();

            PlayerConnection playerConnection = new PlayerConnection(connection) {
                @Override
                public void close() {
                    super.close();
                    handleDisconnect(playerAtomicReference.get());
                }
            };

            ServerPlayer player = new ServerPlayer(world, id, name, playerConnection);
            playerAtomicReference.set(player);
            player.setPositionInternal(new Vector(2, 2));

            //Send position/velocity
            playerConnection.sendPacket(new PacketOutEntityPositionVelocity(id, player.getPosition(), player.getVelocity()));

            //Send block/entity palettes
            Map<Integer, String> blockPalette = new HashMap<>();
            GameBlock.getBlockNameMap().forEach((blockName, block) -> blockPalette.put(GameBlock.getId(block), blockName));
            playerConnection.sendPacket(new PacketOutSetPalette(blockPalette, entityPalette.getPalette()));

            player.joinWorld();

            if (!name.equals(info.name)) {
                player.sendMessage("Your name was changed to " + name);
                player.sendMessage("because there is already a " + info.name);
            }

            world.getPlayers().forEach(p -> ((ServerPlayer) p).sendMessage(player.getName() + " joined the server."));
            System.out.println("JOIN: " + player.getName() + " joined the server.");
            player.sendMessage("BTW do /help for commands");
            player.sendMessage("And also you press T for chat");

            player.sendTitle("Heeyyy! welcome!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Random rand = new Random();

    @EventSubscription
    private void onInteract(PlayerInteractEvent event) {

        world.setBlockAt((int) event.getPosition().getX(), (int) event.getPosition().getY(), GameBlockType.WALL);

        //Fire a bullet
        EntityBullet bullet = new EntityBullet(world);

        bullet.setPositionInternal(event.getPlayer().getPosition());
        bullet.setVelocityInternal(event.getPosition().subtract(event.getPlayer().getPosition()).normalize().multiply(0.5)
                .add(event.getPlayer().getVelocity()));
        bullet.setShouldDoMovementPrediction(true);
        bullet.joinWorld();
    }

    @EventSubscription
    private void onMove(PlayerMoveEvent event) {
        ServerPlayer player = event.getPlayer();
        Vector newPos = event.getNewPosition();
        Vector oldPos = event.getOldPosition();
        int border = 80;
        boolean result = newPos.getX() > -border && newPos.getX() < border && newPos.getY() > -border && newPos.getY() < border;
        if (!result) {
            for (int i = 0; i < 8 + player.getVelocity().length() * 10; i++) {
                EntityFire fire = new EntityFire(world);
                fire.setPositionInternal(oldPos.add(newPos).divide(2D));
                fire.setVelocityInternal(newPos.subtract(oldPos).normalize().multiply(0.26).add(new Vector(rand.nextDouble() * 0.4 - 0.2, rand.nextDouble() * 0.4 - 0.2)));
                fire.size = 5;
                fire.getDataWatcher().set(EntityFire.W_SIZE, fire.size, 0);
                fire.color = (0xCC << 24) | (0 & 255) << 16 | (100 & 255) << 8 | (150 & 255);
                fire.getDataWatcher().set(EntityFire.W_COLOR, fire.color, 0);
                fire.setShouldDoMovementPrediction(true);
                fire.joinWorld();
            }
            player.setVelocity(oldPos.subtract(newPos).multiply(0.15D).add(oldPos.subtract(newPos).normalize().multiply(0.1D)));

            EntityLine line = new EntityLine(world);
            line.setColour(0xCCAA45BA);
            line.setThickness(5);
            line.setPositionInternal(new Vector(-border, -border));
            line.setShape(new Vector(0, border * 2));
            line.joinWorld();

            line = new EntityLine(world);
            line.setColour(0xCCAA45BA);
            line.setThickness(5);
            line.setPositionInternal(new Vector(-border, -border));
            line.setShape(new Vector(border * 2, 0));
            line.joinWorld();

            line = new EntityLine(world);
            line.setColour(0xCCAA45BA);
            line.setThickness(5);
            line.setPositionInternal(new Vector(border, border));
            line.setShape(new Vector(0, -border * 2));
            line.joinWorld();

            line = new EntityLine(world);
            line.setColour(0xCCAA45BA);
            line.setThickness(5);
            line.setPositionInternal(new Vector(border, border));
            line.setShape(new Vector(-border * 2, 0));
            line.joinWorld();
        }
        event.setCancelled(!result);
    }

    private List<Vector> mazeEndPoints = new ArrayList<>();
    private int mazeCounter = 0;
//    private int heartCounter = 0;
//    private int heartBlockCounter = 0;
//
//    private String[] heartBlocks = "5 7, 6 7, 7 7, 8 8, 9 9, 9 10, 10 8, 11 7, 12 7, 13 7, 14 8, 15 9, 15 10, 15 11, 4 12, 4 13, 5 14, 6 15, 7 16, 7 17, 8 18, 9 19, 10 18, 11 17, 11 16, 12 15, 13 14, 14 13, 14 12".split(", ");
//
//    public void tickHeart() {
//        if(heartCounter++ % 4 == 0) {
//            if(heartBlockCounter >= heartBlocks.length) {
//                heartBlockCounter = 0;
//            }
//
//            String[] s = heartBlocks[heartBlockCounter].split(" ");
//            world.setBlockAt(Integer.parseInt(s[0]), Integer.parseInt(s[1]), GameBlockType.WALL);
//
//            int n = heartBlockCounter + 10;
//            n %= heartBlocks.length;
//
//            s = heartBlocks[n].split(" ");
//            world.setBlockAt(Integer.parseInt(s[0]), Integer.parseInt(s[1]), GameBlockType.AIR);
//
//            heartBlockCounter++;
//        }
//    }

    public void tickMaze() {
        if (mazeEndPoints.isEmpty()) {
            return;
        }

        if (++mazeCounter % 8 != 0 || mazeCounter < 60)
            return;

        List<Chunk> c = new ArrayList<>();

        while (mazeEndPoints.size() > 10)
            mazeEndPoints.remove(0);

        for (Vector mazeEndPoint : new ArrayList<>(mazeEndPoints)) {
            Vector v = mazeEndPoint.add(new Vector(0, 1));
            if (world.getBlockAt(v) == GameBlockType.AIR) {
                world.setBlockAt(v, GameBlockType.LIGHT_GREEN);
                Chunk chunk = world.getChunkAt((int) v.getX() >> 4, (int) v.getY() >> 4);
                if (!c.contains(chunk)) {
                    c.add(chunk);
                }
                mazeEndPoints.add(v);
            }

            v = mazeEndPoint.add(new Vector(1, 0));
            if (world.getBlockAt(v) == GameBlockType.AIR) {
                Chunk chunk = world.getChunkAt((int) v.getX() >> 4, (int) v.getY() >> 4);
                if (!c.contains(chunk)) {
                    c.add(chunk);
                }
                world.setBlockAt(v, GameBlockType.LIGHT_GREEN);
                mazeEndPoints.add(v);
            }

            v = mazeEndPoint.add(new Vector(0, -1));
            if (world.getBlockAt(v) == GameBlockType.AIR) {
                world.setBlockAt(v, GameBlockType.LIGHT_GREEN);
                Chunk chunk = world.getChunkAt((int) v.getX() >> 4, (int) v.getY() >> 4);
                if (!c.contains(chunk)) {
                    c.add(chunk);
                }
                mazeEndPoints.add(v);
            }

            v = mazeEndPoint.add(new Vector(-1, 0));
            if (world.getBlockAt(v) == GameBlockType.AIR) {
                world.setBlockAt(v, GameBlockType.LIGHT_GREEN);
                Chunk chunk = world.getChunkAt((int) v.getX() >> 4, (int) v.getY() >> 4);
                if (!c.contains(chunk)) {
                    c.add(chunk);
                }
                mazeEndPoints.add(v);
            }

            mazeEndPoints.remove(mazeEndPoint);


        }
        for (Chunk chunk : c) {
            PacketOutMapChunk mapChunk = new PacketOutMapChunk(chunk);
            world.getPlayers().forEach(p -> {
                if (((ServerPlayer) p).getChunkMap().isLoaded((int) chunk.getPosition().getX(), (int) chunk.getPosition().getY()))
                    p.getConnection().sendPacket(mapChunk);
            });
        }

    }
}
