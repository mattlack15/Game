package me.gravitinos.aigame.common.blocks;

public class GameBlockType {

    public static final GameBlock AIR;
    public static final GameBlock WALL;

    static {
        AIR = new BlockAir();
        GameBlock.registerBlock(AIR, "game.air");

        WALL = new BlockWall();
        GameBlock.registerBlock(WALL, "game.wall");
    }

    public static void init() {
    }
}
