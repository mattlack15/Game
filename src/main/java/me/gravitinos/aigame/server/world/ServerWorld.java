package me.gravitinos.aigame.server.world;

import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.entity.EntityPlayer;
import me.gravitinos.aigame.common.map.Chunk;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.packet.PacketOutSpawnPlayer;

public class ServerWorld extends GameWorld {
    public ServerWorld(String name) {
        super(name);
    }

    @Override
    protected void initChunk(Chunk chunk) {
        chunk.setBlock(2, 2, GameBlock.getBlock(1));
    }

    @Override
    public synchronized void playerJoinWorld(EntityPlayer player) {
        super.playerJoinWorld(player);
        PacketOutSpawnPlayer packetOutSpawnPlayer = new PacketOutSpawnPlayer(player.getId(), player.getName(), player.getPosition(), player.getVelocity());
        this.getPlayers().forEach(p -> {
            if(!p.getId().equals(player.getId())) {
                p.getConnection().sendPacket(packetOutSpawnPlayer);
                player.getConnection().sendPacket(new PacketOutSpawnPlayer(p.getId(), p.getName(), p.getPosition(), p.getVelocity()));
            }
        });
    }

    @Override
    public synchronized void playerLeaveWorld(EntityPlayer player) {
        super.playerLeaveWorld(player);
    }
}
