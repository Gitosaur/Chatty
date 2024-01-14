package crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class CryptoUtils {


    public static String b64encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] b64decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    public static byte[] sha256(String s) {

        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(s.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

}
