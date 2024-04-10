import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Scanner;

public class ATM {
    static int nonce = 1 + (int)(Math.random() * 99999.0);
    static String atmNonce;

    //reads the number from tmp.txt, which represents the current ATM id
    //inelegant solution to a simple file numbering problem..
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

        //creating keypair
        KeyPair keyPair = generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate(); //privateKey only used locally in one instance of ATM

        //storing public keys in file
        System.out.println("creating publicKey file");
        ObjectOutputStream publicStream = new ObjectOutputStream(new FileOutputStream("ATM" + getATMID() + "PublicKey.xx"));
        publicStream.writeObject(publicKey);
        publicStream.close();


        try(
            Socket kkSocket = new Socket(hostName, portNumber);
            PrintWriter output= new PrintWriter(kkSocket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
        ){
            //getting bank public key from file
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("BankPublicKey.xx"));
            PublicKey bankKey = (PublicKey) inputStream.readObject();
            System.out.println("THIS IS KDC'S PUBLIC KEY: " + bankKey.toString() + "\n");
            inputStream.close();

            //creating rsa class for encryption/decryption
            RSA rsa = new RSA();

            //begin master key distribution/authentication protocol here:


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
