package me.gravitinos.aigame.client;

import lombok.Getter;
import me.gravitinos.aigame.Main;
import me.gravitinos.aigame.client.packet.PacketHandlerClient;
import me.gravitinos.aigame.client.player.ClientPlayer;
import me.gravitinos.aigame.client.player.PacketProviderPlayer;
import me.gravitinos.aigame.client.render.block.BlockRender;
import me.gravitinos.aigame.client.render.entity.EntityRender;
import me.gravitinos.aigame.common.packet.*;
import me.gravitinos.aigame.common.util.SharedPalette;
import me.gravitinos.aigame.client.world.ClientWorld;
import me.gravitinos.aigame.common.RegistryInitializer;
import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.connection.PlayerConnection;
import me.gravitinos.aigame.common.connection.SecuredTCPClient;
import me.gravitinos.aigame.common.entity.*;
import me.gravitinos.aigame.common.item.ItemStack;
import me.gravitinos.aigame.common.util.Vector;

import javax.crypto.NoSuchPaddingException;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.List;

public class GameClient {

    public GameClient instance;

    @Getter
    private JFrame frame;

    public static final int TICKS_PER_SECOND = 50;

    public static final int CAMERA_WIDTH_PIXELS = 850;
    public static final int CAMERA_HEIGHT_PIXELS = 750;
    public static final double DEFAULT_SCALE = 1D;

    public GameClient() {
        instance = this;
        this.initWindow();
        this.mainMenu();
    }

    private List<Integer> pressedKeys = new ArrayList<>();

    public ClientWorld world;
    public PlayerCamera camera;
    public ClientPlayer player;
    public SharedPalette<GameBlock> blockPalette = new SharedPalette<>();
    public SharedPalette<String> entityPalette = new SharedPalette<>();

    public void mainMenu() {
        while (true) {
            frame.getGraphics().setColor(Color.DARK_GRAY);
            frame.getGraphics().fillRect(0, 0, frame.getWidth(), frame.getHeight());

            String name = JOptionPane.showInputDialog(frame, "Enter your username, (Or EXIT to close)");
            if(name.equalsIgnoreCase("exit")) {
                frame.dispose();
                return;
            }
            String ip = JOptionPane.showInputDialog(frame, "Enter the server's IP or localhost");
            if(ip.equalsIgnoreCase("") || ip.equals("localhost")) {
                Main.startServer();
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                initWorld(name, ip, 42070);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Could not connect to server.");
                continue;
            }

            try {
                mainHeartbeat();
            } catch (IllegalStateException e) {
                JOptionPane.showMessageDialog(frame, "Disconnected: " + e.getMessage());
            }
        }
    }

    public void initWorld(String username, String remote, int remotePort) throws Exception {
        //remote = "192.168.2.173";

        SecuredTCPClient client;
        client = new SecuredTCPClient(remote, remotePort);
        PlayerConnection connection = new PlayerConnection(client.getConnection());

        this.camera = new PlayerCamera(new Vector(0, 0), PlayerCamera.scale(CAMERA_WIDTH_PIXELS, DEFAULT_SCALE), PlayerCamera.scale(CAMERA_HEIGHT_PIXELS, DEFAULT_SCALE), DEFAULT_SCALE);
        this.world = new ClientWorld("World", this);

        //Default palette
        Map<Integer, GameBlock> palette = new HashMap<>();
        GameBlock.getBlocks().forEach((b) -> palette.put(GameBlock.getId(b), b));
        blockPalette.setPalette(palette);

        player = new ClientPlayer(world, UUID.randomUUID(), connection);
        player.setName(username);

        connection.sendPacket(new PacketInPlayerInfo(player.getId(), username));
        Packet packet = connection.nextPacket();
        if (!(packet instanceof PacketOutEntityPositionVelocity))
            return;
        PacketOutEntityPositionVelocity posVel = (PacketOutEntityPositionVelocity) packet;
        player.setPositionInternal(posVel.position);
        player.setVelocityInternal(posVel.velocity);

        player.checkCollisions = true;

        player.client = this;

        player.joinWorld();

        player.setShouldDoMovementPrediction(true);
        player.setPosition(new Vector(0, 4));
    }

    public void initWindow() {

        RegistryInitializer.init();
        ClientRegistryInitializer.init();

        frame = new JFrame("Game") {
            public void paint(Graphics g) {
                BufferedImage img = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
                _render(img.getGraphics());
                g.drawImage(img, 0, 0, null);
            }
        };
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(CAMERA_WIDTH_PIXELS, CAMERA_HEIGHT_PIXELS);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                player.getConnection().sendPacket(new PacketInDisconnect());
                player.getConnection().close();
                System.exit(0);
            }
        });

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
                    if (e.getKeyCode() == KeyEvent.VK_T || e.getKeyCode() == KeyEvent.VK_SLASH) {
                        if (!player.getChatBox().isTyping()) {
                            player.getChatBox().clearCurrentLine();
                            player.getChatBox().give.set(true);
                            player.getChatBox().setTyping(true);
                            pressedKeys.clear();
                            if(e.getKeyCode() == KeyEvent.VK_SLASH) {
                                player.getChatBox().addChar('/');
                            }
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
                    if(camera != null) {
                        player.interact.set(true);
                    }
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
        double m = 0;
        boolean waitTillNextTick = false;
        long lastFpsCount = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - lastFpsCount >= 1000) {
                currentFPS = fpsCounter;
                currentTPS = tpsCounter;
                fpsCounter = 0;
                tpsCounter = 0;
                lastFpsCount = System.currentTimeMillis();
            }
            fpsCounter++;

            long sinceLastLoop = System.currentTimeMillis() - lastLoop;
            lastLoop = System.currentTimeMillis();
            double multiplier = sinceLastLoop / (1000D / TICKS_PER_SECOND);


            boolean temp = false;
            if(nextTickWait - (System.currentTimeMillis() - lastTick) < 5) {
                multiplier = 1 - m;
                if(!waitTillNextTick) {
                    waitTillNextTick = true;
                    temp = true;
                }
            }
            m += multiplier;

            double finalMultiplier = multiplier;
            if(!waitTillNextTick || temp) {
                world.entityCollection().forEach(e -> {
                    if (e.shouldDoMovementPrediction())
                        e.tick1(finalMultiplier);
                });
            }

            updatePlayer(multiplier * 0.1);

            long ms = System.currentTimeMillis();
            if (ms - lastTick >= nextTickWait) {
                tick();
                tpsCounter++;

                waitTillNextTick = false;

                m = 0;

                nextTickWait = (1000 / TICKS_PER_SECOND) - (Math.min((System.currentTimeMillis() - lastTick), 500) - nextTickWait);
                lastTick = System.currentTimeMillis();
            }

            render();
            try {
                //Thread.sleep(0, 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void render() {
        frame.paint(frame.getGraphics());
    }

    int ibc = 0;

    public String title = null;
    public int titleFadeInTicks = 0;
    public int titleFadeOutTicks = 0;
    public int titleTicksLeft = 0;
    public int titleColour = 0;

    private void _render(Graphics graphics) {
        long ms = System.currentTimeMillis();

        //Render blocks
        if(camera == null)
            return;
        camera.setWidth(PlayerCamera.scale(frame.getWidth(), camera.getScale()));
        camera.setHeight(PlayerCamera.scale(frame.getHeight(), camera.getScale()));
        double width = camera.scale(camera.getWidth());
        double height = camera.scale(camera.getHeight());

        //For all blocks on the screen
        double xMax = width + (camera.getScale() * PlayerCamera.BASE_SCALE_MULTIPLIER * 2);
        double yMax = height + (camera.getScale() * PlayerCamera.BASE_SCALE_MULTIPLIER * 2);
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
        int sY = 30;
        graphics.drawString("Game v1.0 by Matthew Lack", 10, (sY += 20));
        graphics.drawString(String.format("X Coordinate: %.2f", (player.getPosition().getX())), 10, (sY += 20));
        graphics.drawString(String.format("Y Coordinate: %.2f", (player.getPosition().getY())), 10, (sY += 20));
        graphics.drawString("FPS: " + currentFPS, 10, (sY += 20));
        graphics.drawString("TPS: " + currentTPS, 10, (sY += 20));
        graphics.drawString("Scale: " + camera.getScale(), 10, (sY += 20));
        graphics.drawString("Speed: " + player.getVelocity().distance(new Vector(0, 0)) * 20D * 3600 / 1000D + " km/h", 10, (sY += 20));

        player.getChatBox().draw(graphics, frame.getWidth(), frame.getHeight());

        if(title != null) {

            graphics.setFont(new Font("", Font.BOLD, 56));

            int titleWidth = graphics.getFontMetrics().stringWidth(title);
            int titleHeight = graphics.getFontMetrics().getHeight();
            int titleX = frame.getWidth() / 2 - titleWidth / 2;
            int titleY = frame.getHeight() / 2 + titleHeight / 2;

            Color color = new Color(titleColour, true);

            graphics.setColor(color);
            graphics.drawString(title, titleX, titleY);
        }
        ms = System.currentTimeMillis() - ms;

    }

    private void receivePackets() {
        //Receive packets
        List<Packet> received = new ArrayList<>();
        while (!player.getConnection().isClosed() && player.getConnection().hasNextPacket()) {
            received.add(player.getConnection().nextPacket());
        }

        for (Packet packet : received) {
            if(packet instanceof PacketOutRemoteDisconnect) {
                throw new IllegalStateException(((PacketOutRemoteDisconnect) packet).reason);
            }
            PacketHandlerClient handler = PacketHandlerClient.REGISTRY.get(packet.getClass());
            if (handler == null) {
                System.out.println("Could not handle packet type: " + packet.getClass().getName());
                continue;
            }
            handler.handlePacket(packet, this);
        }
    }

    public void tick() {
        world.tick();

        world.getEntities().forEach(GameEntity::tick);

        if(title != null) {
            int opacity = titleColour >>> 24;

            if(opacity == 0 && titleTicksLeft == 0) {
                title = null;
            } else {
                if (opacity != 255 && titleTicksLeft > 0) {
                    if(titleFadeInTicks == 0) {
                        titleFadeInTicks = 1;
                    }
                    opacity = Math.min(opacity + Math.max(255 / titleFadeInTicks, 1), 255);
                    titleColour &= 0xFFFFFF;
                    titleColour |= opacity << 24;
                } else if (titleTicksLeft > 0) {
                    titleTicksLeft--;
                } else {
                    if(titleFadeOutTicks == 0) {
                        titleFadeOutTicks = 1;
                    }
                    opacity = Math.max(opacity - Math.max(255 / titleFadeOutTicks, 1), 0);
                    titleColour &= 0xFFFFFF;
                    titleColour |= opacity << 24;
                }
            }

        }

        receivePackets();

        //Send packets
        PacketProviderPlayer packetProviderPlayer = new PacketProviderPlayer();
        List<Packet> packets = packetProviderPlayer.getPackets(player, player.getDataWatcher()).self;
        if(player.getConnection().isClosed())
            throw new IllegalStateException("Disconnected!");
        packets.forEach((p) -> player.getConnection().sendPacket(p));

    }

    public void updatePlayer(double multiplier) {
        Random random = new Random(System.currentTimeMillis());

        synchronized (this) {
            double speed = pressedKeys.contains(KeyEvent.VK_CONTROL) ? 0.6D * 0.4 : 0.28D * 0.4;
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
            if (pressedKeys.contains(KeyEvent.VK_C)) {
                player.checkCollisions = false;
            } else {
                player.checkCollisions = true;
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
                    fire.joinWorld();
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
