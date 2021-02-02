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
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatBox {

    private static int WIDTH_PIXELS = 300;
    private static int LENGTH_PIXELS = 180;
    private static int LINE_HEIGHT = 30;


    @Getter
    @Setter
    private volatile boolean typing = false;

    private StringBuilder currentLine = new StringBuilder();

    public AtomicBoolean give = new AtomicBoolean();

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

    public void draw(Graphics graphics, int width, int height) {

        int locX = width - WIDTH_PIXELS - 10;

        int i = height - 50;
        graphics.setFont(new Font("chat", Font.PLAIN, 16));

        if (isTyping()) {
            i += 32;

            graphics.setColor(new Color(Color.DARK_GRAY.getRGB() & 0xFFFFFF | (0x88 << 24), true));
            graphics.fillRect(locX - 5, i - graphics.getFont().getSize(), WIDTH_PIXELS, graphics.getFont().getSize() + 6);

            graphics.setColor(Color.WHITE);

            graphics.drawString(" > " + getCurrentLine(), locX, i);

            i -= 32;
        }

        int maxLineWidth = 260;

        List<String> adjustedLines = new ArrayList<>();

        for (String line : lines) {

            line = "&&FFFFFF" + line;

            List<String> temp = new ArrayList<>();

            //Split into lines that match max line width
            StringBuilder builder = new StringBuilder();
            String[] words = line.split(" ");
            for (int j = 0; j < words.length; j++) {
                if (graphics.getFontMetrics().stringWidth(builder + " " + words[j]) > maxLineWidth) {

                    temp.add(builder.toString());
                    builder = new StringBuilder(words[j]);
                } else {
                    if (j != 0) {
                        builder.append(" ");
                    }
                    builder.append(words[j]);
                }
            }
            if (builder.length() > 0) {
                temp.add(builder.toString());
            }

            for (int j = temp.size() - 1; j >= 0; j--) {
                adjustedLines.add(temp.get(j));
                i -= graphics.getFont().getSize() + 4;
            }
            if (adjustedLines.size() >= 5 && !isTyping()) {
                break;
            }
        }

        int a = 0;
        int colour = 0xFFFFFF;

        for (int i1 = adjustedLines.size() - 1; i1 >= 0; i1--) {
            i += graphics.getFont().getSize() + 4;
            String s = adjustedLines.get(i1);

            graphics.setColor(new Color(Color.DARK_GRAY.getRGB() & 0xFFFFFF | (0x88 << 24), true));
            graphics.fillRect(locX - 5, i - graphics.getFont().getSize(), WIDTH_PIXELS, graphics.getFont().getSize() + 4);

            graphics.setColor(Color.WHITE);

            colour = renderColouredText(graphics, s, locX, i, colour, 255);


        }
    }

    public static int renderColouredText(Graphics graphics, String s, int x, int y) {
        return renderColouredText(graphics, s, x, y, 0xFFFFFF, 255);
    }

    public static int renderColouredText(Graphics graphics, String s, int x, int y, int startingColour, int opacity) {
        int colour = startingColour | opacity << 24;

        String[] parts = s.split("&&");

        int xPos = x;
        for (int j = 0; j < parts.length; j++) {
            if (j != 0) {
                try {
                    colour = Integer.parseInt(parts[j].substring(0, 6), 16) | opacity << 24;
                    parts[j] = parts[j].substring(6);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                }
            }
            graphics.setColor(new Color(colour, true));
            graphics.drawString(parts[j], xPos, y);
            int w = graphics.getFontMetrics().stringWidth(parts[j]);
            xPos += w;
        }
        return colour;
    }

    public static String removeChatColours(String s) {
        StringBuilder builder = new StringBuilder();
        String[] parts = s.split("&&");
        for (int i = 0; i < parts.length; i++) {
            if(i == 0) {
                builder.append(parts[i]);
            } else {
                if(parts[i].length() < 6) {
                    builder.append(parts[i]);
                    continue;
                }
                builder.append(parts[i].substring(6));
            }
        }
        return builder.toString();
    }

}
