package me.gravitinos.aigame.client.render.block;

import me.gravitinos.aigame.common.util.BlockVector;
import me.gravitinos.aigame.common.util.Vector;

import java.awt.*;

public class BlockRenderLightGreen extends BlockRender {
    @Override
    public void draw(Graphics graphics, BlockVector position, double scaleFactor) {
        graphics.setColor(new Color(Color.GRAY.getRGB() & 0xFFFFFF | 0x45 << 24, true));
        //graphics.clearRect((int) position.getX(), (int) position.getY(), (int) scaleFactor, (int) scaleFactor);
        graphics.fillRect((int) position.getX(), (int) position.getY(), (int) scaleFactor, (int) scaleFactor);
    }
}
