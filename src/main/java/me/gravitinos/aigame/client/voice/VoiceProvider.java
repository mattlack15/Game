package me.gravitinos.aigame.client.voice;

import me.gravitinos.aigame.Main;
import me.gravitinos.aigame.common.connection.Packet;
import me.gravitinos.aigame.common.packet.PacketInOutAudio;

import javax.sound.sampled.*;
import java.util.zip.Deflater;

public class VoiceProvider implements AutoCloseable {
    private TargetDataLine line;
    private boolean enabled = false;
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

    public PacketInOutAudio getPacket() {

        if(!active)
            return null;

        //Get audio
        int avail = line.available();

        if(avail == 0)
            return null;

        byte[] b = new byte[avail];
        line.read(b, 0, b.length);

        //Compress
//        Deflater deflater = new Deflater();
//        deflater.setInput(b);
//        deflater.finish();
//        int am = deflater.deflate(b);
//        deflater.end();
//
//        //Resize
//        if(b.length != am) {
//            byte[] old = b;
//            b = new byte[am];
//            System.arraycopy(old, 0, b, 0, b.length);
//        }

        //Create and return packet
        return new PacketInOutAudio(b, Main.getAudioFormat().getFrameSize(), false);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isActive() {
        return active;
    }

    public void start() {
        line.start();
        line.flush();
        active = true;
    }

    public void stop() {
        line.stop();
        line.flush();
        active = false;
    }

    @Override
    public void close() {
        line.close();
    }

}
