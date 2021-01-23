package me.gravitinos.aigame.server.world;

import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.map.Chunk;
import me.gravitinos.aigame.common.map.GameWorld;

public class ServerWorld extends GameWorld {
    public ServerWorld(String name) {
        super(name);
    }

    @Override
    protected void initChunk(Chunk chunk) {
        chunk.setBlock(2, 2, GameBlock.getBlock(1));
    }
}
