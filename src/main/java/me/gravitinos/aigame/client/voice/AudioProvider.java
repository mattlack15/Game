package me.gravitinos.aigame.client.voice;

import me.gravitinos.aigame.Main;

import javax.sound.sampled.*;

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
    }

    private boolean playing = false;

    public void play(byte[] arr) {
        if(!playing) {
            playing = true;
            line.start();
        }
        line.write(arr, 0, Math.min(arr.length, line.available()));
    }

    public void tick() {
        if(line.getBufferSize() == line.available()) {
            line.stop();
            playing = false;
        }
    }

    @Override
    public void close() {
        line.close();
    }
}
