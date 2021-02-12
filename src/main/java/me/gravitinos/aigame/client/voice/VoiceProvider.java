package me.gravitinos.aigame.client.voice;

import me.gravitinos.aigame.Main;
import me.gravitinos.aigame.common.packet.PacketPlayAudio;
import net.ultragrav.serializer.compressors.StandardCompressor;

import javax.sound.sampled.*;
import java.util.zip.Deflater;

public class VoiceProvider implements AutoCloseable {
    private TargetDataLine line;
    private boolean enabled = false;
    int frameSize = Main.getAudioFormat().getFrameSize();
    private boolean active = false;

    public VoiceProvider() {
        AudioFormat format = Main.getAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            return;
        }

        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
        } catch (LineUnavailableException ex) {
            return;
        }

        enabled = true;
    }

    public synchronized PacketPlayAudio getPacket() {

        if(!active)
            return null;

        //Get audio
        int avail = line.available();

        if(avail == 0)
            return null;

        byte[] b = new byte[avail];
        line.read(b, 0, b.length);

        b = StandardCompressor.instance.compress(b);

        //Create and return packet
        return new PacketPlayAudio(b, frameSize, true);
    }

    public synchronized boolean isEnabled() {
        return enabled;
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized void start() {
        if(active)
            return;
        line.start();
        line.flush();
        active = true;
    }

    public synchronized void stop() {
        if(!active)
            return;
        line.stop();
        line.flush();
        active = false;
    }

    @Override
    public void close() {
        line.close();
    }

}
