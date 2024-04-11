import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

public class MAC {

    //create a Message Authentication Code (MAC) using the message it will be sent with, and the MASTER KEY
    public String createMAC(String message, SecretKey masterKey) throws Exception {

        //Creating and initializing Mac object
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(masterKey);

        byte[] bytes = message.getBytes();
        byte[] macResult = mac.doFinal(bytes);

        return macResult.toString();
    }

}