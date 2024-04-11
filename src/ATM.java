import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Base64;
import java.util.Objects;
import java.util.Scanner;

public class ATM {
    static int nonce = 1 + (int)(Math.random() * 99999.0);

    //reads the number from tmp.txt, which represents the current ATM id
    //inelegant solution to a simple file numbering problem.. it only supports up to 10 instances of ATM
    private static int getATMID() throws IOException {
        FileReader fr = new FileReader("tmp.txt");
        BufferedReader bufferedReader = new BufferedReader(fr);
        int id;
        id = fr.read();
        System.out.println((Character.getNumericValue((char)id)));
        id = Character.getNumericValue((char)id);
        int currentATMID = id;
        id++;
        FileWriter fw = new FileWriter("tmp.txt");
        //System.out.println(id);
        fw.write(Integer.toString(id));
        fw.close();
        return currentATMID;
    }

    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        return keyPairGenerator.generateKeyPair();
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        String hostName = "localhost";
        int portNumber = 4444;
        String ATMID = Integer.toString(getATMID());
        //creating keypair
        KeyPair keyPair = generateKeyPair();
        PublicKey ATMPublicKey = keyPair.getPublic();
        PrivateKey ATMPrivateKey = keyPair.getPrivate(); //privateKey only used locally in one instance of ATM

        //storing public keys in file
        System.out.println("creating publicKey file");
        ObjectOutputStream publicStream = new ObjectOutputStream(new FileOutputStream("ATM" + ATMID + "PublicKey.xx"));
        publicStream.writeObject(ATMPublicKey);
        publicStream.close();


        try(
            Socket kkSocket = new Socket(hostName, portNumber);
            PrintWriter output= new PrintWriter(kkSocket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
        ){
            //getting bank public key from file
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("BankPublicKey.xx"));
            PublicKey bankPublicKey = (PublicKey) inputStream.readObject();
            //System.out.println("THIS IS BANK'S PUBLIC KEY: " + bankKey.toString() + "\n");
            inputStream.close();

            //creating rsa class for encryption/decryption
            RSA rsa = new RSA();

            //telling bank server what public key file to read;
            output.println(ATMID);

            //begin master key distribution/authentication protocol here:
            //receive bank nonce and bank id, decrypt with ATM private key
            String toDecryptBankNonce = input.readLine();
            String toDecryptBankID = input.readLine();

            String decryptedBankNonce = rsa.privateDecrypt(toDecryptBankNonce, ATMPrivateKey);
            String decryptedBankID = rsa.privateDecrypt(toDecryptBankID, ATMPrivateKey);
            System.out.println("\n||Received Bank Nonce: " + decryptedBankNonce + "||\nencrypted="+toDecryptBankNonce);
            System.out.println("||Received ID: " + decryptedBankID + "||\nencrypted="+toDecryptBankID);


            //Send bank nonce and ATM nonce, encrypted with bank public key
            String ATMNonce = Integer.toString(nonce);
            String encryptedBankNonce = rsa.publicEncrypt(decryptedBankNonce, bankPublicKey);
            String encryptedATMNonce = rsa.publicEncrypt(ATMNonce, bankPublicKey);

            output.println(encryptedBankNonce);
            output.println(encryptedATMNonce);
            System.out.println("\n||Sending Bank Nonce: " + decryptedBankNonce + "||\nencrypted="+encryptedBankNonce);
            System.out.println("||Sending ATM Nonce: " + ATMNonce + "||\nencrypted="+encryptedATMNonce);

            //Receive ATM Nonce, encrypted with ATM public key
            String toDecryptATMNonce = input.readLine();
            String decryptedATMNonce = rsa.privateDecrypt(toDecryptATMNonce, ATMPrivateKey);
            System.out.println("\n||Received ATM Nonce: " + decryptedATMNonce + "||\nencrypted="+toDecryptATMNonce);


            //Receive Master Key, decrypt with ATM private key, then decrypt with bank public key
            int index = 0;
            String incoming;
            String[] doubleEncrypt = new String[3];
            while(!Objects.equals(incoming = input.readLine(), "stop")){
                doubleEncrypt[index] = incoming;
                index++;
            }

            String decryptedFirstHalf = rsa.privateDecrypt(doubleEncrypt[0], ATMPrivateKey);
            String decryptedSecondHalf = rsa.privateDecrypt(doubleEncrypt[1], ATMPrivateKey);
            String combined = decryptedFirstHalf.concat(decryptedSecondHalf);
            String masterKeyString = rsa.publicDecrypt(combined, bankPublicKey);
            //recreating master key
            byte[] decodedMasterKey = Base64.getDecoder().decode(masterKeyString);
            SecretKey masterKey = new SecretKeySpec(decodedMasterKey, 0, decodedMasterKey.length, "DES");
            System.out.println("||Received Master Key from the Bank: " + masterKeyString+"||");


            //after authentication call GUI forms:


        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
