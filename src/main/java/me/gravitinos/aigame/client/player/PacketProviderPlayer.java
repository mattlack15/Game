package me.gravitinos.aigame.client.player;

import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.datawatcher.DataWatcher;
import me.gravitinos.aigame.common.datawatcher.PacketPackage;
import me.gravitinos.aigame.common.datawatcher.PacketProvider;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.packet.PacketInPlayerMove;
import me.gravitinos.aigame.common.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class PacketProviderPlayer extends PacketProvider<ClientPlayer> {
    @Override
    public PacketPackage getPackets(ClientPlayer player, DataWatcher dataWatcher) {
        PacketPackage packets = new PacketPackage();

        if(dataWatcher.setDirt(GameEntity.W_POSITION, 0) > 0) {
            Vector position = dataWatcher.get(GameEntity.W_POSITION);
            Vector previous = dataWatcher.get(GameEntity.W_LAST_POSITION);
            dataWatcher.set(GameEntity.W_LAST_POSITION, position);
            if(previous == null) {
                previous = new Vector(0, 0);
            }
            Vector dPos = position.subtract(previous);
            packets.self.add(new PacketInPlayerMove(dPos));
        }

        return packets;
    }
}
