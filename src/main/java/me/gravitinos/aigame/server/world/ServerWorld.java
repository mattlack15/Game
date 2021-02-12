package me.gravitinos.aigame.server.world;

import me.gravitinos.aigame.common.blocks.GameBlock;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.entity.EntityPlayer;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.map.Chunk;
import me.gravitinos.aigame.common.map.GameWorld;
import me.gravitinos.aigame.common.packet.PacketOutDestroyEntity;
import me.gravitinos.aigame.common.packet.PacketOutSpawnEntity;
import me.gravitinos.aigame.common.packet.PacketOutSpawnPlayer;
import me.gravitinos.aigame.common.util.SharedPalette;

import java.util.HashSet;

public class ServerWorld extends GameWorld {
    private SharedPalette<String> entityPalette;
    private HashSet<Chunk> updated = new HashSet<>(); //TODO add block-change packet functionality
    public ServerWorld(String name, SharedPalette<String> entityPalette) {
        super(name);
        this.entityPalette = entityPalette;
    }

    @Override
    public synchronized void setBlockAt(int x, int y, GameBlock block) {
        super.setBlockAt(x, y, block);
        updated.add(getChunkAt(x >> 4, y >> 4));
    }

    public synchronized HashSet<Chunk> getAndClearUpdated() {
        HashSet<Chunk> out = updated;
        updated = new HashSet<>();
        return out;
    }

    @Override
    public synchronized void entityJoinWorld(GameEntity entity) {
        super.entityJoinWorld(entity);
        PacketOutSpawnEntity packet = new PacketOutSpawnEntity(entity, entityPalette);
        getPlayers().forEach(p -> p.getConnection().sendPacket(packet));
    }

    @Override
    public synchronized void entityLeaveWorld(GameEntity entity) {
        super.entityLeaveWorld(entity);
        PacketOutDestroyEntity packet = new PacketOutDestroyEntity(entity.getId());
        getPlayers().forEach(p -> {
            try {
                p.getConnection().sendPacket(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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
        Packet packetDestroy = new PacketOutDestroyEntity(player.getId());
        getPlayers().forEach(p -> p.getConnection().sendPacket(packetDestroy));
    }
}
