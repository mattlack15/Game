package me.gravitinos.aigame.client.voice;

import me.gravitinos.aigame.Main;
import me.gravitinos.aigame.common.packet.PacketPlayAudio;
import net.ultragrav.serializer.GravSerializer;

import javax.sound.sampled.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class AudioProvider implements AutoCloseable {
    private boolean enabled = false;
    private SourceDataLine line;

    public AudioProvider() {
        AudioFormat format = Main.getAudioFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            return;
        }

        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
        } catch (LineUnavailableException ex) {
            return;
        }

        enabled = true;
        new Thread(() -> {
            while(true) {
                try {
                    flushQueue();
                    tick();
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Audio Provider");
    }

    private boolean playing = false;
    private Queue<PacketPlayAudio> queue = new ConcurrentLinkedQueue<>();

    public void flushQueue() {
        PacketPlayAudio packetPlayAudio;
        while((packetPlayAudio = queue.poll()) != null) {
            handleAudioPacket0(packetPlayAudio);
        }
    }

    public void queueAudioPacket(PacketPlayAudio audioPacket) {
        queue.offer(audioPacket);
    }

    private void handleAudioPacket0(PacketPlayAudio audioPacket) {
        byte[] data = audioPacket.data;

        //Decompress if needed
        if(audioPacket.compressed) {
            Inflater inflater = new Inflater();
            GravSerializer serializer = new GravSerializer();

            inflater.setInput(data);
            byte[] buf = new byte[2048];
            while(!inflater.finished()) {
                try {
                    int am = inflater.inflate(buf);
                    if(am == 0) {
                        return;
                    }
                    serializer.append(buf, am);
                } catch (DataFormatException e) {
                    e.printStackTrace();
                }
            }
            data = serializer.toByteArray();
        }

        play(data);
    }

    public synchronized void play(byte[] arr) {
        if(!playing) {
            playing = true;
            line.start();
        }
        line.write(arr, 0, Math.min(arr.length, line.available()));
    }

    public synchronized void tick() {
        if(line.getBufferSize() == line.available()) {
            line.stop();
            line.flush();
            playing = false;
        }
    }

    @Override
    public synchronized void close() {
        line.close();
    }
}
