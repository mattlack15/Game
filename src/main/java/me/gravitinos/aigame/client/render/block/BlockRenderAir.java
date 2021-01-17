package me.gravitinos.aigame.client.render.block;

import me.gravitinos.aigame.client.PlayerCamera;
import me.gravitinos.aigame.common.util.Vector;

import java.awt.*;

public class BlockRenderAir extends BlockRender {
    @Override
    public void draw(Graphics graphics, Vector position, double scaleFactor) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect((int) position.getX(), (int) position.getY(), (int) scaleFactor, (int) scaleFactor);
    }
}
