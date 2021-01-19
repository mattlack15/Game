package me.gravitinos.aigame.common.connection;

import net.ultragrav.serializer.GravSerializer;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SecuredTCPServer implements AutoCloseable {
    private ServerSocket serverSocket;

    private List<SecuredTCPConnection> connectionList = Collections.synchronizedList(new ArrayList<>());
    private KeyPair keyPair;
    private Cipher cipherRSA;
    private int port;

    public SecuredTCPServer(int port) {
        this.port = port;
    }


    public void start() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException {
        keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        cipherRSA = Cipher.getInstance("RSA");
        serverSocket = new ServerSocket(port);

        System.out.println("Server started on " + serverSocket.getInetAddress().getHostName());

        new Thread(() -> {
            while (true) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Socket finalSocket = socket;
                new Thread(() -> {
                    try {
                        SecuredTCPConnection connection = new SecuredTCPConnection(finalSocket);
                        connection.next();

                        GravSerializer serializer = new GravSerializer();
                        serializer.writeString("RSA");
                        serializer.writeByteArray(keyPair.getPublic().getEncoded());

                        connection.send(serializer);

                        connection.setEncryptionProvider(new EncryptionProvider("RSA", keyPair.getPrivate()));

                        serializer = connection.next();
                        String algo = serializer.readString();
                        SecretKey key = new SecretKeySpec(serializer.readByteArray(), algo);
                        EncryptionProvider encryptionProvider = new EncryptionProvider(algo, key);
                        connection.setEncryptionProvider(encryptionProvider);

                        handleConnection(connection);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }).start();
    }

    public void handleConnection(SecuredTCPConnection connection) {

    }


    @Override
    public void close() throws Exception {
        serverSocket.close();
    }
}
