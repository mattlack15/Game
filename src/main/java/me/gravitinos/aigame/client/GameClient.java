package me.gravitinos.aigame.client;

import lombok.Getter;
import me.gravitinos.aigame.client.packet.PacketHandlerClient;
import me.gravitinos.aigame.client.player.ClientPlayer;
import me.gravitinos.aigame.client.player.PacketProviderPlayer;
import me.gravitinos.aigame.client.render.block.BlockRender;
import me.gravitinos.aigame.client.render.entity.EntityRender;
import me.gravitinos.aigame.client.world.ClientWorld;
import me.gravitinos.aigame.common.RegistryInitializer;
import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.connection.PlayerConnection;
import me.gravitinos.aigame.common.connection.SecuredTCPClient;
import me.gravitinos.aigame.common.entity.*;
import me.gravitinos.aigame.common.item.ItemStack;
import me.gravitinos.aigame.common.packet.PacketInOutChatMessage;
import me.gravitinos.aigame.common.packet.PacketInPlayerInfo;
import me.gravitinos.aigame.common.packet.PacketOutEntityPositionVelocity;
import me.gravitinos.aigame.common.util.Vector;

import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class GameClient {

    public GameClient instance;

    @Getter
    private JFrame frame;

    public static final int TICKS_PER_SECOND = 30;

    public static final int CAMERA_WIDTH_PIXELS = 850;
    public static final int CAMERA_HEIGHT_PIXELS = 750;
    public static final double DEFAULT_SCALE = 1D;

    public GameClient() {
        instance = this;
        this.init();
        this.mainHeartbeat();
    }

    private List<Integer> pressedKeys = new ArrayList<>();

    public ClientWorld world;
    public PlayerCamera camera;
    public ClientPlayer player;

    public void init() {

        RegistryInitializer.init();
        ClientRegistryInitializer.init();

        String remote = "127.0.0.1";
        int remotePort = 6969;

        SecuredTCPClient client;
        try {
            client = new SecuredTCPClient(remote, remotePort);
        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        PlayerConnection connection = new PlayerConnection(client.getConnection());
        Random random = new Random(System.currentTimeMillis());

        this.camera = new PlayerCamera(new Vector(0, 0), PlayerCamera.scale(CAMERA_WIDTH_PIXELS, DEFAULT_SCALE), PlayerCamera.scale(CAMERA_HEIGHT_PIXELS, DEFAULT_SCALE), DEFAULT_SCALE);
        this.world = new ClientWorld("World", this);

        player = new ClientPlayer(world, UUID.randomUUID(), connection);

        connection.sendPacket(new PacketInPlayerInfo(player.getId(), player.getId().toString().substring(0, 3)));
        Packet packet = connection.nextPacket();
        if (!(packet instanceof PacketOutEntityPositionVelocity))
            return;
        PacketOutEntityPositionVelocity posVel = (PacketOutEntityPositionVelocity) packet;
        player.setPositionInternal(posVel.position);
        player.setVelocityInternal(posVel.velocity);

        //Create player
        //world.getChunkAt((int) Math.floor(player.getPosition().getX()) >> 4, (int) Math.floor(player.getPosition().getY()) >> 4)
        //        .addEntity(player);

        frame = new JFrame("Game") {
            public void paint(Graphics g) {
                BufferedImage img = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
                _render(img.getGraphics());
                g.drawImage(img, 0, 0, null);
            }
        };
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(CAMERA_WIDTH_PIXELS, CAMERA_HEIGHT_PIXELS);
        frame.setVisible(true);

        frame.setIgnoreRepaint(true);

        //Key Listeners
        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (player.getChatBox().isTyping() && !player.getChatBox().give.compareAndSet(true, false)) {
                    if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
                        if (player.getChatBox().getCurrentBuilder().length() > 0)
                            player.getChatBox().getCurrentBuilder().deleteCharAt(player.getChatBox().getCurrentBuilder().length() - 1);
                    } else {
                        player.getChatBox().addChar(e.getKeyChar());
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                synchronized (GameClient.this) {
                    if (pressedKeys.contains(e.getKeyCode()))
                        return;
                    if (!player.getChatBox().isTyping())
                        pressedKeys.add(e.getKeyCode());
                    if (e.getKeyCode() == KeyEvent.VK_T) {
                        if (!player.getChatBox().isTyping()) {
                            player.getChatBox().give.set(true);
                            player.getChatBox().setTyping(true);
                            pressedKeys.clear();
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        String message = player.getChatBox().getCurrentLine();
                        PacketInOutChatMessage chatMessage = new PacketInOutChatMessage(message);
                        player.getConnection().sendPacket(chatMessage);
                        player.getChatBox().clearCurrentLine();
                        player.getChatBox().setTyping(false);
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        player.getChatBox().setTyping(false);
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                synchronized (GameClient.this) {
                    pressedKeys.remove((Integer) e.getKeyCode());
                }
            }
        });

        //Mouse Listener
        frame.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {

            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        frame.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {

                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        player.setPosition(new Vector(0, 4));
    }

    @Getter
    private int mouseX = 0;
    @Getter
    private int mouseY = 0;

    private double currentFPS = 0D;
    private double currentTPS = 0D;
    private long nextTickWait = 1000 / TICKS_PER_SECOND;

    public void mainHeartbeat() {
        long lastTick = System.currentTimeMillis() - (1000 / TICKS_PER_SECOND);
        long lastLoop = System.currentTimeMillis();
        int fpsCounter = 0;
        int tpsCounter = 0;
        long lastFpsCount = System.currentTimeMillis();
        while (true) {
            long sinceLastLoop = System.currentTimeMillis() - lastLoop;
            if (System.currentTimeMillis() - lastFpsCount >= 1000) {
                currentFPS = fpsCounter;
                currentTPS = tpsCounter;
                fpsCounter = 0;
                tpsCounter = 0;
                lastFpsCount = System.currentTimeMillis();
            }
            fpsCounter++;
            lastLoop = System.currentTimeMillis();

            long ms = System.currentTimeMillis();
            long sinceLast = System.currentTimeMillis() - lastTick;
            if (ms - lastTick >= nextTickWait) {
                tick();
                tpsCounter++;

                nextTickWait = (1000 / TICKS_PER_SECOND) - ((ms - lastTick) - nextTickWait);
                lastTick = System.currentTimeMillis();
            }

            double multiplier = sinceLastLoop / (1000D / TICKS_PER_SECOND);

            world.getEntities().forEach(e -> e.tick1(multiplier));

            updatePlayer(multiplier * 0.1);

            long tick1Ms = System.currentTimeMillis();

            render();

            tick1Ms = System.currentTimeMillis() - tick1Ms;
            //System.out.println(tick1Ms);
            try {
                Thread.sleep(0, 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void render() {
        frame.paint(frame.getGraphics());
    }

    int ibc = 0;

    private void _render(Graphics graphics) {
        long ms = System.currentTimeMillis();

        //Render blocks
        camera.setWidth(PlayerCamera.scale(frame.getWidth(), camera.getScale()));
        camera.setHeight(PlayerCamera.scale(frame.getHeight(), camera.getScale()));
        double width = camera.scale(camera.getWidth());
        double height = camera.scale(camera.getHeight());

        //For all blocks on the screen
        double xMax = width + camera.getScale() * PlayerCamera.BASE_SCALE_MULTIPLIER;
        double yMax = height + camera.getScale() * PlayerCamera.BASE_SCALE_MULTIPLIER;
        for (int x = 0; x <= xMax; x += camera.getScale() * PlayerCamera.BASE_SCALE_MULTIPLIER) {
            for (int y = 0; y <= yMax; y += camera.getScale() * PlayerCamera.BASE_SCALE_MULTIPLIER) {

                //Get in-game position
                Vector pos = camera.fromScreenCoordinates(new Vector(x, y)).floor();

                //Get block
                if (world.getLoadedChunkAt((int) pos.getX() >> 4, (int) pos.getY() >> 4) == null)
                    continue;

                GameBlock block = world.getBlockAt(pos);

                //Get renderer
                BlockRender renderer = BlockRender.REGISTRY.get(block);

                //Get screen position
                Vector screenPos = camera.toScreenCoordinates(pos);

                //Render
                renderer.draw(graphics, screenPos, camera.getScale() * PlayerCamera.BASE_SCALE_MULTIPLIER);
                y--;
            }
            x--;
        }

        double num = width + camera.getScale() * PlayerCamera.BASE_SCALE_MULTIPLIER;

        //Render entities
        for (GameEntity entity : world.getEntities()) {
            if (entity.getPosition().distanceSquared(camera.getPosition()) < num * num) {
                //TODO
                Class<?> clazz = entity.getClass();
                if (entity instanceof ClientPlayer)
                    clazz = clazz.getSuperclass();
                EntityRender renderer = EntityRender.REGISTRY.get(clazz);
                if (renderer == null) {
                    System.out.println("Could not render entity type: " + entity.getClass().getName());
                    continue;
                }
                renderer.draw(graphics, entity, camera.toScreenCoordinates(entity.getPosition()), camera.getScale() * PlayerCamera.BASE_SCALE_MULTIPLIER);
            }
        }

        //Render Inventory
        int x = 30;
        int y = (int) (frame.getHeight() - PlayerCamera.scale(1.5D, 1D) - 60);

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            graphics.setColor(new Color(Color.LIGHT_GRAY.getRGB() & 0xFFFFFF | (0x55 << 24), true));
            graphics.fillRect(x, y, (int) PlayerCamera.scale(1.5D, 1D), (int) PlayerCamera.scale(1.5D, 1D));
            graphics.setColor(Color.GREEN);
            graphics.drawRect(x, y, (int) PlayerCamera.scale(1.5D, 1D), (int) PlayerCamera.scale(1.5D, 1D));

            ItemStack itemStack = player.getInventory().getContents()[i];

            if (itemStack != null) {
                itemStack.getType().getDrawFunc().accept(new Vector(x + 11, y + 11), graphics, 0.75D);
                graphics.setColor(Color.WHITE);
                graphics.setFont(new Font("none", Font.BOLD, 16));
                graphics.drawString(Integer.toString(itemStack.getAmount()), (int) Math.round(x + PlayerCamera.scale(0.82D, 1D)), (int) Math.round(y + PlayerCamera.scale(1.25D, 1D)));
            }
            x += PlayerCamera.scale(1.75D, 1D);
        }


        //Debug Info
        graphics.setColor(Color.WHITE);
        graphics.drawString("X Coordinate: " + (player.getPosition().getX()), 10, 50);
        graphics.drawString("Y Coordinate: " + (player.getPosition().getY()), 10, 70);
        graphics.drawString("FPS: " + currentFPS, 10, 90);
        graphics.drawString("TPS: " + currentTPS, 10, 110);
        Vector worldPos = camera.fromScreenCoordinates(new Vector(mouseX, mouseY));
        Vector screenPos = camera.toScreenCoordinates(worldPos).round();
        graphics.drawString("Mouse Loc: " + screenPos.getX() + "   " + screenPos.getY(), 10, 130);
        graphics.drawString("Actual Mouse Loc: " + mouseX + "   " + mouseY, 10, 150);
        graphics.drawString("Scale: " + camera.getScale(), 10, 170);
        graphics.drawString("Speed: " + player.getVelocity().distance(new Vector(0, 0)) * 20D * 3600 / 1000D + " km/h", 10, 190);

        player.getChatBox().draw(graphics, frame.getWidth(), frame.getHeight());

        ms = System.currentTimeMillis() - ms;

    }

    public void tick() {
        world.tick();

        world.getEntities().forEach(GameEntity::tick);

        //Send packets
        PacketProviderPlayer packetProviderPlayer = new PacketProviderPlayer();
        List<Packet> packets = packetProviderPlayer.getPackets(player, player.getDataWatcher()).self;
        packets.forEach((p) -> player.getConnection().sendPacket(p));

        //Receive packets
        List<Packet> received = new ArrayList<>();
        while (player.getConnection().hasNextPacket()) {
            received.add(player.getConnection().nextPacket());
        }

        for (Packet packet : received) {
            PacketHandlerClient handler = PacketHandlerClient.REGISTRY.get(packet.getClass());
            if (handler == null) {
                System.out.println("Could not handle packet type: " + packet.getClass().getName());
                continue;
            }
            handler.handlePacket(packet, this);
        }

    }

    public void updatePlayer(double multiplier) {
        Random random = new Random(System.currentTimeMillis());

        synchronized (this) {
            double speed = pressedKeys.contains(KeyEvent.VK_CONTROL) ? 0.6D * 0.66D : 0.28D * 0.66D;
            speed *= multiplier;
            Vector posAdd = new Vector(0, 0);
            if (pressedKeys.contains(KeyEvent.VK_A)) {
                posAdd = posAdd.add(new Vector(-1, 0));
            }
            if (pressedKeys.contains(KeyEvent.VK_S)) {
                posAdd = posAdd.add(new Vector(0, 1));
            }
            if (pressedKeys.contains(KeyEvent.VK_D)) {
                posAdd = posAdd.add(new Vector(1, 0));
            }
            if (pressedKeys.contains(KeyEvent.VK_W)) {
                posAdd = posAdd.add(new Vector(0, -1));
            }
            if (posAdd.abs().sum() != 0D) {
                Vector dVel = posAdd.multiply(Math.sqrt(speed * speed / posAdd.abs().sum()));
                player.setVelocity(player.getVelocity().add(dVel));

                Vector pos = player.getPosition();
                pos = pos.add(dVel.normalize().multiply(-0.8D)).add(0.15, 0.15);

                int amount = 8;
                boolean sprint = pressedKeys.contains(KeyEvent.VK_CONTROL);
                if (sprint)
                    amount += 12;

                double spread = (sprint ? 0.9D : 0.6D);

                for (int i = 0; i < amount; i++) {
                    Vector pos1 = pos.add(random.nextDouble() * spread - (spread / 2), random.nextDouble() * spread - (spread / 2));
                    EntityFire fire = new EntityFire(world);
                    fire.setPosition(pos1);
                    fire.setVelocity(player.getVelocity().add(dVel.multiply(-20D)));
                }
            }
            if (pressedKeys.contains(KeyEvent.VK_N)) {
                player.setVelocity(new Vector(0, 0));
            }
            if (pressedKeys.contains(KeyEvent.VK_Z)) {
                camera.setScale(camera.getScale() - 0.02);
            }
            if (pressedKeys.contains(KeyEvent.VK_X)) {
                camera.setScale(camera.getScale() + 0.02);
            }
        }
        camera.setPosition(player.getPosition());
    }

    private static int a = 1;

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        new GameClient();
    }
}
