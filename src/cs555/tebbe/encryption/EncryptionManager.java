package cs555.tebbe.encryption;

import javax.crypto.Cipher;
import java.io.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by ctebbe
 */
public class EncryptionManager {

    public static final String ALGO = "RSA";

    private String PRIVATE;
    private String PUBLIC;
    private String host;

    private PublicKey pubKey;
    private PrivateKey privKey;

    public EncryptionManager(String id) throws IOException, ClassNotFoundException {
        host = id;
        PRIVATE = "./rsa/private-" + host + ".key";
        PUBLIC = "./rsa/public-" + host + ".key";
        if(!areKeysGenerated())
            generateKeys();
        loadKeys();
    }

    public byte[] encrypt(byte[] bytes) {
        byte[] encrypted = null;
        try {
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            encrypted = cipher.doFinal(bytes);
        } catch (Exception e) { e.printStackTrace(); }
        return encrypted;
    }

    public byte[] decrypt(byte[] bytes) {
        byte[] decrypted = null;
        try {
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, privKey);
            decrypted = cipher.doFinal(bytes);
        } catch (Exception e) { e.printStackTrace(); }
        return decrypted;
    }

    public void loadKeys() throws IOException, ClassNotFoundException {
        ObjectInputStream is = null;

        // pub key
        is = new ObjectInputStream(new FileInputStream(PUBLIC));
        pubKey = (PublicKey) is.readObject();
        is.close();

        // priv key
        is = new ObjectInputStream(new FileInputStream(PRIVATE));
        privKey = (PrivateKey) is.readObject();
        is.close();
    }

    private boolean areKeysGenerated() {
        File priv = new File(PRIVATE);
        File pub = new File(PUBLIC);
        return priv.exists() && pub.exists();
    }

    public void generateKeys() {
        System.out.println("GEN KEYS");
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGO);
            keyGen.initialize(1024);
            final KeyPair key = keyGen.generateKeyPair();
            File privateKeyFile = new File(PRIVATE);
            File publicKeyFile = new File(PUBLIC);

            if (privateKeyFile.getParentFile() != null) {
                privateKeyFile.getParentFile().mkdirs();
            }
            privateKeyFile.createNewFile();

            if (publicKeyFile.getParentFile() != null) {
                publicKeyFile.getParentFile().mkdirs();
            }
            publicKeyFile.createNewFile();

            ObjectOutputStream publicKeyOS = new ObjectOutputStream(
                    new FileOutputStream(publicKeyFile));
            publicKeyOS.writeObject(key.getPublic());
            publicKeyOS.close();

            ObjectOutputStream privateKeyOS = new ObjectOutputStream(
                    new FileOutputStream(privateKeyFile));
            privateKeyOS.writeObject(key.getPrivate());
            privateKeyOS.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
