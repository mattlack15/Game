package me.gravitinos.aigame.common.blocks;

import java.awt.*;

public class Air extends GameBlock {

    public static Image texture;
    static {
        String path = "C:\\Users\\mateo\\Downloads\\GRASS_BLOCK_TOP.png";
        texture = Toolkit.getDefaultToolkit().getImage(path);
        texture.flush();
    }

    @Override
    public boolean isSolid() {
        return false;
    }
}
