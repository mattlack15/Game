package me.gravitinos.aigame.server.packet.provider;

import me.gravitinos.aigame.common.datawatcher.DataWatcher;
import me.gravitinos.aigame.common.datawatcher.PacketPackage;
import me.gravitinos.aigame.common.datawatcher.PacketProvider;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.map.Chunk;
import me.gravitinos.aigame.common.packet.PacketOutEntityPositionVelocity;
import me.gravitinos.aigame.common.packet.PacketOutMapChunk;
import me.gravitinos.aigame.common.util.Vector;
import me.gravitinos.aigame.server.player.PlayerChunkMap;
import me.gravitinos.aigame.server.player.ServerPlayer;

public class PacketProviderServerPlayer extends PacketProvider<ServerPlayer> {
    @Override
    public PacketPackage getPackets(ServerPlayer player, DataWatcher dataWatcher) {
        PacketPackage packets = new PacketPackage();

        int pos = dataWatcher.setDirt(GameEntity.W_POSITION, 0);
        int vel = dataWatcher.setDirt(GameEntity.W_VELOCITY, 0);
        if(pos > 0 || vel > 0) {
            Vector position = dataWatcher.get(GameEntity.W_POSITION);
            Vector velocity = dataWatcher.get(GameEntity.W_VELOCITY);

            PacketOutEntityPositionVelocity packet = new PacketOutEntityPositionVelocity(player.getId(), position, velocity);
            packets.other.add(packet);
            if(pos > 1 || vel > 1) { //Dirt level is above 1, meaning it is required to send to the player as well
                packets.self.add(packet);
                player.setTpConfirmation(packet.pId);
            }
        }

        if(pos > 0) {

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
                            packets.self.add(new PacketOutMapChunk(chunk));
                            chunkMap.setLoaded(x, y);
                        }
                    }
                }
            }
        }

        return packets;
    }
}
