package me.gravitinos.aigame.client.packet;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.packet.PacketInOutAudio;
import net.ultragrav.serializer.GravSerializer;

import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class PacketHandlerAudio implements PacketHandlerClient {
    @Override
    public void handlePacket(Packet packet, GameClient client) {
        PacketInOutAudio audioPacket = (PacketInOutAudio) packet;
        byte[] data = audioPacket.data;

        //Decompress if needed
        if(audioPacket.compressed) {
            Inflater inflater = new Inflater();
            GravSerializer serializer = new GravSerializer();

            inflater.setInput(data);
            byte[] buf = new byte[2048];
            while(!inflater.finished()) {
                try {
                    int am = inflater.inflate(buf, 0, buf.length);
                    serializer.append(buf, am);
                } catch (DataFormatException e) {
                    e.printStackTrace();
                }
            }
            data = serializer.toByteArray();
        }

        client.audioProvider.play(data);
    }
}
