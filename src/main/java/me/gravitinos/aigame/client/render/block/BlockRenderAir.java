package me.gravitinos.aigame.client.render.block;

import me.gravitinos.aigame.client.PlayerCamera;
import me.gravitinos.aigame.common.util.Vector;

import java.awt.*;

public class BlockRenderAir extends BlockRender {
    @Override
    public void draw(Graphics graphics, Vector position, double scaleFactor) {
        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect((int) position.getX(), (int) position.getY(), (int) scaleFactor, (int) scaleFactor);
        graphics.setColor(Color.BLACK);
        graphics.drawLine((int) position.getX(), (int) position.getY(), (int) position.getX() + (int) scaleFactor, (int) position.getY());
        graphics.drawLine((int) position.getX(), (int) position.getY(), (int) position.getX(), (int) position.getY() + (int) position.getY());

    }
}
