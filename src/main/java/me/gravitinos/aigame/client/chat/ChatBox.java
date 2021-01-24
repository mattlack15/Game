package me.gravitinos.aigame.client.chat;

import lombok.Getter;
import lombok.Setter;
import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.client.PlayerCamera;
import me.gravitinos.aigame.client.Renderable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatBox implements Renderable {

    private static int WIDTH_PIXELS = 300;
    private static int LENGTH_PIXELS = 180;
    private static int LINE_HEIGHT = 30;

    private int width;
    private int height;

    public ChatBox(int screenWidth, int screenHeight) {
        this.width = screenWidth;
        this.height = screenHeight;
    }

    @Getter
    @Setter
    private volatile boolean typing = false;

    private StringBuilder currentLine = new StringBuilder();

    @Getter
    private List<String> lines = Collections.synchronizedList(new ArrayList<>());

    public synchronized void addChar(char c) {
        currentLine.append(c);
    }

    public synchronized StringBuilder getCurrentBuilder() {
        return this.currentLine;
    }

    public synchronized String getCurrentLine() {
        return currentLine.toString();
    }

    public synchronized void clearCurrentLine() {
        this.currentLine = new StringBuilder();
    }

    public void chat(String message) {
        lines.add(0, message);
    }

    @Override
    public void draw(Graphics graphics, PlayerCamera camera) {

        int locX = width - WIDTH_PIXELS - 10;

        int i = height - 50;
        graphics.setFont(new Font("chat", Font.PLAIN, 16));

        if(isTyping()) {
            i += 32;

            graphics.setColor(new Color(Color.DARK_GRAY.getRGB() & 0xFFFFFF | (0x88 << 24), true));
            graphics.fillRect(locX - 5, i - graphics.getFont().getSize(), WIDTH_PIXELS, graphics.getFont().getSize() + 6);

            graphics.setColor(Color.WHITE);

            graphics.drawString(" > " + getCurrentLine(), locX, i);

            i -= 32;
        }

        for(String s : lines) {

            graphics.setColor(new Color(Color.DARK_GRAY.getRGB() & 0xFFFFFF | (0x88 << 24), true));
            graphics.fillRect(locX - 5, i - graphics.getFont().getSize(), WIDTH_PIXELS, graphics.getFont().getSize() + 4);

            graphics.setColor(Color.WHITE);

            graphics.drawString(s, locX, i);
            i -= graphics.getFont().getSize() + 4;
        }
    }
}
