import java.io.*;
import java.net.*;
import java.security.*;
import java.util.ArrayList;

public class BankServer {

    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        return keyPairGenerator.generateKeyPair();
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        //initialize tmp.txt file at start of bank service
        FileWriter fw = new FileWriter("tmp.txt");
        fw.write("1");
        fw.close();


        //creating public and private key files for the bank server threads to access
        KeyPair keyPair = generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        System.out.println("creating publicKey file");
        ObjectOutputStream publicStream = new ObjectOutputStream(new FileOutputStream("BankPublicKey.xx"));
        publicStream.writeObject(publicKey);
        publicStream.close();
        System.out.println("creating privateKey file");
        ObjectOutputStream privateStream = new ObjectOutputStream(new FileOutputStream("BankPrivateKey.xx"));
        privateStream.writeObject(privateKey);
        privateStream.close();

        int portNumber = 4444;
        boolean listening = true;

        //threads
        ArrayList<BankServerThread> threadList = new ArrayList<>();
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (listening) {
                System.out.println("Listening on port " + portNumber);
                Socket socket = serverSocket.accept();
                BankServerThread BankMultiServer = new BankServerThread(socket, threadList);
                threadList.add(BankMultiServer);
                BankMultiServer.start();

            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }

}
