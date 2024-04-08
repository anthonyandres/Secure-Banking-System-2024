import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class RSA {
    Cipher encryptCipher = Cipher.getInstance("RSA");
    Cipher decryptCipher = Cipher.getInstance("RSA");

    RSA() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    }

    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(128);
        return keyPairGenerator.generateKeyPair();
    }

    public String publicEncrypt(String toEncrypt, PublicKey publicKey) throws Exception {
        this.encryptCipher.init(1, publicKey);
        byte[] utf8 = toEncrypt.getBytes("UTF8");
        byte[] encrypted = this.encryptCipher.doFinal(utf8);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String privateEncrypt(String toEncrypt, PrivateKey privateKey) throws Exception {
        this.encryptCipher.init(1, privateKey);
        byte[] utf8 = toEncrypt.getBytes("UTF8");
        byte[] encrypted = this.encryptCipher.doFinal(utf8);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String publicDecrypt(String toDecrypt, PublicKey publicKey) throws Exception {
        this.decryptCipher.init(2, publicKey);
        byte[] decrypt = Base64.getDecoder().decode(toDecrypt);
        byte[] output = this.decryptCipher.doFinal(decrypt);
        return new String(output, "UTF8");
    }

    public String privateDecrypt(String toDecrypt, PrivateKey privateKey) throws Exception {
        this.decryptCipher.init(2, privateKey);
        byte[] decrypt = Base64.getDecoder().decode(toDecrypt);
        byte[] output = this.decryptCipher.doFinal(decrypt);
        return new String(output, "UTF8");
    }

    public static void main(String[] args) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        RSA rsa = new RSA();
        String msg = "hello world!";
        String encrypt = rsa.publicEncrypt(msg, publicKey);
        System.out.println(encrypt);
        String decrypt = rsa.privateDecrypt(encrypt, privateKey);
        System.out.println(decrypt);
        System.out.println("Public key:" + Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        System.out.println("Private key:" + Base64.getEncoder().encodeToString(privateKey.getEncoded()));
    }
}
