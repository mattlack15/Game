package me.gravitinos.aigame.common.connection;

import javax.crypto.Cipher;
import java.security.Key;

public class EncryptionProvider {
    private Key key;
    String algorithm;

    public EncryptionProvider(String algorithm, Key key) {
        this.algorithm = algorithm;
        this.key = key;
    }

    public byte[] encrypt(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] decrypt(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
