package me.gravitinos.aigame.client.player;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.datawatcher.DataWatcher;
import me.gravitinos.aigame.common.datawatcher.PacketProvider;
import me.gravitinos.aigame.common.entity.EntityPlayer;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.packet.PacketInPlayerMove;
import me.gravitinos.aigame.common.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class PacketProviderPlayer extends PacketProvider {
    @Override
    public List<Packet> getPackets(DataWatcher dataWatcher) {
        List<Packet> packets = new ArrayList<>();

        if(dataWatcher.setDirty(GameEntity.W_POSITION, false)) {
            Vector position = dataWatcher.get(GameEntity.W_POSITION);
            Vector previous = dataWatcher.get(GameEntity.W_LAST_POSITION);
            dataWatcher.set(GameEntity.W_LAST_POSITION, position);
            if(previous == null) {
                previous = new Vector(0, 0);
            }
            Vector dPos = position.subtract(previous);
            packets.add(new PacketInPlayerMove(dPos));
        }

        return packets;
    }
}
