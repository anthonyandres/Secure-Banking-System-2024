import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;

public class MAC {

    //create a Message Authentication Code (MAC) using the message it will be sent with, and the MASTER KEY
    public String createMAC(String message, Key masterKey) throws Exception {
        //Creating a KeyGenerator object
        KeyGenerator keyGen = KeyGenerator.getInstance("DES");

        //Creating a SecureRandom object
        SecureRandom secRandom = new SecureRandom();

        //Initializing the KeyGenerator
        keyGen.init(secRandom);

        //Creating and initializing Mac object
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(masterKey);

        byte[] bytes = message.getBytes();
        byte[] macResult = mac.doFinal(bytes);

        return macResult.toString();
    }

}