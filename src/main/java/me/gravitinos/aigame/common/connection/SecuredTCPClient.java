package me.gravitinos.aigame.common.connection;

import net.ultragrav.serializer.GravSerializer;

import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class SecuredTCPClient implements AutoCloseable {
    private SecuredTCPConnection connection;

    public SecuredTCPClient(String remote, int remotePort) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        connection = new SecuredTCPConnection(new Socket(remote, remotePort));
        initEncryption();
    }

    private void initEncryption() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException {

        //Create desired encryption provider
        String targetAlgorithm = "AES";
        SecretKey targetKey = KeyGenerator.getInstance(targetAlgorithm).generateKey();
        EncryptionProvider target = new EncryptionProvider(targetAlgorithm, targetKey);

        //Send encryption request
        GravSerializer serializer = new GravSerializer();
        serializer.writeString("REQ::ENCRYPT");
        connection.send(serializer);

        //Receive encryption key
        serializer = connection.next();
        String algorithm = serializer.readString();

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(serializer.readByteArray());

        PublicKey key = KeyFactory.getInstance(algorithm).generatePublic(keySpec);
        EncryptionProvider p = new EncryptionProvider(algorithm, key);

        //Set encryption
        connection.setEncryptionProvider(p);

        //Send target key
        serializer = new GravSerializer();
        serializer.writeString(targetAlgorithm);
        serializer.writeByteArray(targetKey.getEncoded());
        connection.send(serializer);

        //Set to target encryption
        connection.setEncryptionProvider(target);
    }

    public SecuredTCPConnection getConnection() {
        return this.connection;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
