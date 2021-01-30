package me.gravitinos.aigame.common.blocks;

public class GameBlockType {

    public static final GameBlock AIR = GameBlock.registerBlock(new BlockAir(), "game.air");
    public static final GameBlock WALL = GameBlock.registerBlock(new BlockWall(), "game.wall");
    public static final GameBlock LIGHT_GREEN = GameBlock.registerBlock(new BlockLightGreen(), "game.light_green");

    public static void init() {
    }
}
