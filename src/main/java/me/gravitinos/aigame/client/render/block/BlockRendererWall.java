package me.gravitinos.aigame.client.render.block;

import me.gravitinos.aigame.common.util.BlockVector;
import me.gravitinos.aigame.common.util.Vector;

import java.awt.*;

public class BlockRendererWall extends BlockRender {
    @Override
    public void draw(Graphics graphics, BlockVector position, double scaleFactor) {
        graphics.setColor(new Color(0xC1043C));
        graphics.fillRect(position.getX(), position.getY(), (int) scaleFactor, (int) scaleFactor);
    }
}
