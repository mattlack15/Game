package me.gravitinos.aigame.common.blocks;

public class GameBlockType {

    public static final GameBlock AIR;
    public static final GameBlock WALL;

    static {
        AIR = new BlockAir();
        GameBlock.registerBlock(AIR);

        WALL = new BlockWall();
        GameBlock.registerBlock(WALL);
    }

    public static void init() {
    }
}
