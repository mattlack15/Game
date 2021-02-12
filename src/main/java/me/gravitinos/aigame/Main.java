package me.gravitinos.aigame;

import me.gravitinos.aigame.client.GameClient;
import me.gravitinos.aigame.server.GameServer;

import javax.crypto.NoSuchPaddingException;
import javax.sound.sampled.*;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Deflater;

public class Main {

    public static AudioFormat getAudioFormat() {
        float sampleRate = 32000;
        int sampleSizeInBits = 8;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                channels, signed, bigEndian);
        return format;
    }

    public static void main(String[] args) throws LineUnavailableException {
        //startServer();
        startClient();
    }

    public static void startServer() {
        new Thread(() -> {
            try {
                new GameServer(42070).start();
            } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException e) {
                e.printStackTrace();
            }
        }, "Server Thread").start();
    }

    public static void startClient() {
        new GameClient();
    }
}
