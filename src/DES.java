import java.security.Key;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class DES {
    Cipher encryptCipher = Cipher.getInstance("DES");
    Cipher decryptCipher = Cipher.getInstance("DES");
    //DES(SecretKey key) throws Exception {
    DES(Key masterKey) throws Exception {
        this.encryptCipher.init(1, masterKey);
        this.decryptCipher.init(2, masterKey);
    }

    public String encrypt(String toEncrypt) throws Exception {
        byte[] utf8 = toEncrypt.getBytes("UTF8");
        byte[] encrypted = this.encryptCipher.doFinal(utf8);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decrypt(String toDecrypt) throws Exception {
        byte[] decrypt = Base64.getDecoder().decode(toDecrypt);
        byte[] output = this.decryptCipher.doFinal(decrypt);
        return new String(output, "UTF8");
    }
}
