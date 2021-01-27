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
import me.gravitinos.aigame.common.entity.EntityFire;
import me.gravitinos.aigame.common.entity.EntityPlayer;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.map.Chunk;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.packet.*;
import me.gravitinos.aigame.common.util.SharedPalette;
import me.gravitinos.aigame.common.util.Vector;
import me.gravitinos.aigame.server.minigame.MazeGenerator;
import me.gravitinos.aigame.server.packet.handler.*;
import me.gravitinos.aigame.server.packet.provider.PacketProviderServerPlayer;
import me.gravitinos.aigame.server.player.ServerPlayer;
import me.gravitinos.aigame.server.world.ServerWorld;

import javax.crypto.NoSuchPaddingException;
import java.awt.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
    }

    private void initRegistries() {
        RegistryInitializer.init();
        PacketHandlerServer.REGISTRY.put(PacketInPlayerMove.class, new PacketHandlerPlayerMove());
        PacketHandlerServer.REGISTRY.put(PacketInOutChatMessage.class, new PacketHandlerChatMessage());
        PacketHandlerServer.REGISTRY.put(PacketInPositionConfirmation.class, new PacketHandlerPositionConfirmation());
        PacketHandlerServer.REGISTRY.put(PacketInDisconnect.class, new PacketHandlerDisconnect());
    }

    private AtomicBoolean stopping = new AtomicBoolean(false);

    private void mainLoop() {

        long lastTick;

        System.out.println("Press ENTER to stop the server.");

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
            try {
                if(System.in.available() > 0) {
                    stop();
                    break;
                }
                if(stopping.get()) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if(stopping.compareAndSet(false, true)) {
            System.out.println("Stopping server.");

            //TODO send kick packets

            //TODO save chunks

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

        //Receive packets
        receivePackets();

        //Send packets
        sendPackets();

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
            } catch(Exception e) {
                handleDisconnect((ServerPlayer) player);
                System.out.println(player.getName() + " was disconnected.");
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
                    if (!worldPlayer.getId().equals(player.getId())) {
                        if(worldPlayer.getConnection().isClosed())
                            throw new IllegalStateException();
                        worldPlayer.getConnection().sendPacket(packet);
                    }
                }
            }
        }
    }

    public void handleDisconnect(ServerPlayer player) {
        world.playerLeaveWorld(player);
        Packet packetDestroy = new PacketOutDestroyEntity(player.getId());
        world.getPlayers().forEach(p -> {
            p.getConnection().sendPacket(packetDestroy);
            ((ServerPlayer)p).sendMessage(player.getName() + " left the server.");
        });
        player.getConnection().close();
        System.out.println("DISCONNECT: " + player.getName() + " disconnected.");
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

            if(pos2.distanceSquared(player.getPosition()) < pos1.distanceSquared(player.getPosition())) {
                player.setPosition(pos2.add(1, 1));
            } else {
                player.setPosition(pos1.add(-1, -1));
            }
        } else if(cmd.equalsIgnoreCase("maze")) {
            long ms = System.currentTimeMillis();
            int[][] maze = MazeGenerator.generate(101);
            long genMs = System.currentTimeMillis() - ms;
            MazeGenerator.placeMaze(maze, this, -60, -60);
            for (EntityPlayer worldPlayer : world.getPlayers()) {
                worldPlayer.setPosition(new Vector(-58.9, -58.9));
            }
            ms = System.currentTimeMillis() - ms;
            player.sendMessage("Created a 41x41 maze in " + ms + "ms. (gen " + genMs + "ms)");
        }
    }

    @Override
    public void handleConnection(SecuredTCPConnection connection) {
        try {
            PacketInPlayerInfo info = connection.nextPacket();
            UUID id = info.id;
            String name = info.name;

            int i = 2; //Start at name2 then name3 then name4 etc
            while(world.getPlayer(name) != null) {
                name = info.name + (i++);
            }

            PlayerConnection playerConnection = new PlayerConnection(connection);
            ServerPlayer player = new ServerPlayer(world, id, name, playerConnection);
            player.setPosition(new Vector(2, 2));

            //Send position/velocity
            playerConnection.sendPacket(new PacketOutEntityPositionVelocity(id, player.getPosition(), player.getVelocity()));

            //Send block/entity palettes
            Map<Integer, String> blockPalette = new HashMap<>();
            GameBlock.getBlockNameMap().forEach((blockName, block) -> blockPalette.put(GameBlock.getId(block), blockName));
            playerConnection.sendPacket(new PacketOutSetPalette(blockPalette, entityPalette.getPalette()));

            player.joinWorld();

            if(!name.equals(info.name)) {
                player.sendMessage("Your name was changed to " + name);
                player.sendMessage("because there is already a " + info.name);
            }

            world.getPlayers().forEach(p -> ((ServerPlayer)p).sendMessage(player.getName() + " joined the server."));
            System.out.println("JOIN: " + player.getName() + " joined the server.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Random rand = new Random();

    public boolean onMove(ServerPlayer player, Vector oldPos, Vector newPos) {
        int border = 80;
        boolean result = newPos.getX() > -border && newPos.getX() < border && newPos.getY() > -border && newPos.getY() < border;
        if(!result) {
            for (int i = 0; i < 8 + player.getVelocity().length() * 10; i++) {
                EntityFire fire = new EntityFire(world);
                fire.setPositionInternal(oldPos);
                fire.setVelocityInternal(newPos.subtract(oldPos).normalize().multiply(0.26).add(new Vector(rand.nextDouble()*0.4 - 0.2, rand.nextDouble()*0.4 - 0.2)));
                fire.size = 5;
                fire.getDataWatcher().set(EntityFire.W_SIZE, fire.size, 0);
                fire.color = (0xCC << 24) | (0 & 255) << 16 | (100 & 255) << 8 | (150 & 255);
                fire.getDataWatcher().set(EntityFire.W_COLOR, fire.color, 0);
                fire.setShouldDoMovementPrediction(true);
                fire.joinWorld();
            }
            player.setVelocity(oldPos.subtract(newPos).multiply(0.15D).add(oldPos.subtract(newPos).normalize().multiply(0.1D)));
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        Console console = System.console();
        if(console == null && !GraphicsEnvironment.isHeadless()){
            String filename = GameServer.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
            Runtime.getRuntime().exec(new String[]{"cmd","/c","start","cmd","/k","java -jar \"" + filename + "\""});
        }else{
            main1(new String[0]);
            System.out.println("Program has ended, please type 'exit' to close the console");
        }
    }



    public static void main1(String[] args) {

//        new Thread(() -> {
            try {
                new GameServer(6969).start();
                System.exit(0);
            } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
//        }, "Server Thread").start();
//
//        Thread.sleep(10);
  //      new Thread(GameClient::new).start();

//        Thread.sleep(3000);
//        System.out.println("Next player joining...");
    //    new GameClient();
    }
}
