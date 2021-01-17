package me.gravitinos.aigame.common;

import me.gravitinos.aigame.common.blocks.BlockAir;
import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.blocks.BlockWall;

public class RegistryInitializer {
    public static void init() {
        //Blocks
        GameBlock.registerBlock(new BlockAir());
        GameBlock.registerBlock(new BlockWall());
    }
}
