package me.gravitinos.aigame.common;

import me.gravitinos.aigame.common.blocks.Air;
import me.gravitinos.aigame.common.blocks.GameBlock;

public class RegistryInitializer {
    public static void init() {
        //Blocks
        GameBlock.registerBlock(new Air());
    }
}
