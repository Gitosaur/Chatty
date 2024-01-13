package crypto;

import javax.crypto.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DiffieHellman {

    private KeyPair keyPair;

    public DiffieHellman() {
        keyPair = generateKeyPair();
    }

    private PublicKey getPublicKeyFromBytes(byte[] pkBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("EC");
        X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(pkBytes);
        return kf.generatePublic(pkSpec);
    }

    public KeyPair generateKeyPair() {
        // Generate ephemeral ECDH keypair
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("EC");
            kpg.initialize(256);
            return kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] generateSharedSecret(String otherPublicKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        byte[] otherPublicKeyBytes = CryptoUtils.b64decode(otherPublicKey);
        PublicKey other = getPublicKeyFromBytes(otherPublicKeyBytes);
        // Perform key agreement
        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(this.keyPair.getPrivate());
        ka.doPhase(other, true);
        byte[] sharedSecret = ka.generateSecret();

        MessageDigest hash = MessageDigest.getInstance("SHA-256");
        hash.update(sharedSecret);
        // Simple deterministic ordering
        List<ByteBuffer> keys = Arrays.asList(ByteBuffer.wrap(this.keyPair.getPublic().getEncoded()), ByteBuffer.wrap(other.getEncoded()));
        Collections.sort(keys);
        hash.update(keys.get(0));
        hash.update(keys.get(1));

        return hash.digest();
    }

    public KeyPair getKeyPair() {
        return this.keyPair;
    }
}
