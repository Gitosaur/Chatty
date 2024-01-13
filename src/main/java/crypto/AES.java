package crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

public class AES {

    private static final String cipher = "AES/GCM/PKCS5Padding";
    private static final int TAG_LENGTH = 128;

    private SecretKey key;

    public AES() throws NoSuchAlgorithmException {
        this(generateSecretKey());
    }

    public AES(byte[] bytes) {
        this(new SecretKeySpec(bytes, "AES"));
    }

    public AES(SecretKey key) {
        this.key = key;

    }

    public static SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();
    }

    public String[] encrypt(String message) {
        try {
            Cipher encryptionCipher = Cipher.getInstance(cipher);
            encryptionCipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] bytes = message.getBytes();
            String encryptedMsg = CryptoUtils.b64encode(encryptionCipher.doFinal(bytes));
            String iv = CryptoUtils.b64encode(encryptionCipher.getIV());
            return new String[]{encryptedMsg, iv};
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String decrypt(String message, String iv) {
        byte[] bytes = CryptoUtils.b64decode(message);
        byte[] ivBuffer = CryptoUtils.b64decode(iv);
        try {
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, ivBuffer);
            Cipher decryptCipher = Cipher.getInstance(cipher);
            decryptCipher.init(Cipher.DECRYPT_MODE, key, spec);
            return new String(decryptCipher.doFinal(bytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public SecretKey getSecretKey(){
        return this.key;
    }

}
