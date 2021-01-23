package me.gravitinos.aigame.server.packet.provider;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.datawatcher.DataWatcher;
import me.gravitinos.aigame.common.datawatcher.PacketProvider;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.map.Chunk;
import me.gravitinos.aigame.common.packet.PacketInPlayerMove;
import me.gravitinos.aigame.common.packet.PacketOutEntityPositionVelocity;
import me.gravitinos.aigame.common.packet.PacketOutMapChunk;
import me.gravitinos.aigame.common.util.Vector;
import me.gravitinos.aigame.server.player.PlayerChunkMap;
import me.gravitinos.aigame.server.player.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class PacketProviderServerPlayer extends PacketProvider<ServerPlayer> {

    @Override
    public List<Packet> getPacketsOther(ServerPlayer obj, DataWatcher dataWatcher) {

    }

    @Override
    public List<Packet> getPacketsSelf(ServerPlayer player, DataWatcher dataWatcher) {
        List<Packet> packets = new ArrayList<>();

        boolean pos = dataWatcher.setDirty(GameEntity.W_POSITION, false);
        boolean vel = dataWatcher.setDirty(GameEntity.W_VELOCITY, false);
        if(pos || vel) {
            Vector position = dataWatcher.get(GameEntity.W_POSITION);
            Vector velocity = dataWatcher.get(GameEntity.W_VELOCITY);
            packets.add(new PacketOutEntityPositionVelocity(player.getId(), position, velocity));
        }

        if(pos) {

            //Check chunks
            PlayerChunkMap chunkMap = player.getChunkMap();
            int distance = 8;

            chunkMap.clean(player.getChunkLocation().getX(), player.getChunkLocation().getY(), distance);

            int xMax = player.getChunkLocation().getX() + (distance / 2);
            int yMax = player.getChunkLocation().getY() + (distance / 2);
            for (int x = player.getChunkLocation().getX() - (distance / 2); x <= xMax; x++) {
                for (int y = player.getChunkLocation().getY() - (distance / 2); y < yMax; y++) {
                    if(!chunkMap.isLoaded(x, y)) {
                        Chunk chunk = player.getWorld().getChunkAt(x, y);
                        if(chunk != null) {
                            packets.add(new PacketOutMapChunk(chunk));
                            chunkMap.setLoaded(x, y);
                        }
                    }
                }
            }
        }

        return packets;
    }
}
