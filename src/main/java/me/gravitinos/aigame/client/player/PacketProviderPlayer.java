package me.gravitinos.aigame.client.player;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.datawatcher.DataWatcher;
import me.gravitinos.aigame.common.datawatcher.PacketPackage;
import me.gravitinos.aigame.common.datawatcher.PacketProvider;
import me.gravitinos.aigame.common.entity.GameEntity;
import me.gravitinos.aigame.common.packet.PacketInPlayerInteract;
import me.gravitinos.aigame.common.packet.PacketInPlayerMove;
import me.gravitinos.aigame.common.util.Vector;

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


        //At end
        if(player.interact.compareAndSet(true, false)) {
            Vector pos = new Vector(player.client.getMouseX(), player.client.getMouseY());
            pos = player.client.camera.fromScreenCoordinates(pos);
            pos.floor();
            packets.self.add(new PacketInPlayerInteract(pos.getX(), pos.getY()));
        }

        return packets;
    }
}
