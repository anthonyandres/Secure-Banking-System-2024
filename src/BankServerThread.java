import java.net.*;
import java.io.*;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BankServerThread extends Thread{
    private Socket socket = null;
    private ArrayList<BankServerThread> threadList;
    private PrintWriter output;

    public BankServerThread(Socket socket, ArrayList<BankServerThread> threads) {
        //super("SiriMultiServerThread");
        this.socket = socket;
        this.threadList = threads;
    }

    public void run() {

        try (
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ){
            //read private key file for this thread to get the private key
            ObjectInputStream bankinput = new ObjectInputStream(new FileInputStream("BankPrivateKey.xx"));
            PrivateKey bankPrivateKey = (PrivateKey) bankinput.readObject();
            RSA rsa = new RSA();

            //begin master key distribution protocol here:





            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void printToAllClients(String message) {
        for (BankServerThread sT : threadList) {
            sT.output.println(message);
        }
    }

}
