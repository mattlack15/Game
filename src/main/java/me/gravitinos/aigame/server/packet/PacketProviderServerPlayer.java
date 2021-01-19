package me.gravitinos.aigame.server.packet;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.datawatcher.DataWatcher;
import me.gravitinos.aigame.common.datawatcher.PacketProvider;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.packet.PacketInPlayerMove;
import me.gravitinos.aigame.common.packet.PacketOutEntityPositionVelocity;
import me.gravitinos.aigame.common.util.Vector;
import me.gravitinos.aigame.server.player.PlayerChunkMap;
import me.gravitinos.aigame.server.player.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class PacketProviderServerPlayer extends PacketProvider<ServerPlayer> {
    @Override
    public List<Packet> getPackets(ServerPlayer player, DataWatcher dataWatcher) {
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
            for(int x = )
        }

        return packets;
    }
}
