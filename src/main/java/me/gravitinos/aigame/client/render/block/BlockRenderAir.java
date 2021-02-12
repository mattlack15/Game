package me.gravitinos.aigame.client.render.block;

import me.gravitinos.aigame.client.PlayerCamera;
import me.gravitinos.aigame.common.util.BlockVector;
import me.gravitinos.aigame.common.util.Vector;

import java.awt.*;

public class BlockRenderAir extends BlockRender {
    @Override
    public void draw(Graphics graphics, BlockVector position, double scaleFactor) {
        ((Graphics2D)graphics).setStroke(new BasicStroke(1F));
        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect(position.getX(), position.getY(), (int) scaleFactor, (int) scaleFactor);
        graphics.setColor(Color.BLACK);
        graphics.drawLine(position.getX(), position.getY(), position.getX() + (int) scaleFactor, position.getY());
        graphics.drawLine(position.getX(), position.getY(), position.getX(), position.getY() + (int) scaleFactor);

    }
}
