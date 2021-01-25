package me.gravitinos.aigame.common;

import me.gravitinos.aigame.common.blocks.BlockAir;
import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.blocks.BlockWall;
import me.gravitinos.aigame.common.blocks.GameBlockType;

public class RegistryInitializer {
    public static void init() {
        GameBlockType.init();
    }
}
