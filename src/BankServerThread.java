import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;

public class BankServerThread extends Thread{
    static int nonce = 1 + (int)(Math.random() * 99999.0);
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
            String ATMID = input.readLine();

            if(ATMID.equals("1") || ATMID.equals("2") || ATMID.equals("3") || ATMID.equals("4") || ATMID.equals("5") || ATMID.equals("6") || ATMID.equals("7") || ATMID.equals("8") || ATMID.equals("9") || ATMID.equals("10")) {
                //read private key file for this thread to get the private key
                ObjectInputStream bankinput = new ObjectInputStream(new FileInputStream("BankPrivateKey.xx"));
                PrivateKey bankPrivateKey = (PrivateKey) bankinput.readObject();
                bankinput.close();

                //read public key file for the ATM this thread is dealing with
                //String ATMID = input.readLine();
                ObjectInputStream ATMinput = new ObjectInputStream(new FileInputStream("ATM" + ATMID + "PublicKey.xx"));
                PublicKey ATMPublicKey = (PublicKey) ATMinput.readObject();
                ATMinput.close();

                //read master key file for this thread to get the master key
                ObjectInputStream masterinput = new ObjectInputStream(new FileInputStream("MasterKey.xx"));
                SecretKey masterKey = (SecretKey) masterinput.readObject();
                masterinput.close();

                RSA rsa = new RSA();

                //begin master key distribution protocol here:
                //send bank nonce and id of bank "bank", encrypted with public key of ATM
                String bankNonce = Integer.toString(nonce);
                String bankID = "bank";

                String encryptedBankNonce = rsa.publicEncrypt(bankNonce, ATMPublicKey);
                String encryptedBankID = rsa.publicEncrypt(bankID, ATMPublicKey);

                output.println(encryptedBankNonce);
                output.println(encryptedBankID);
                System.out.println("\n||Sending Nonce: " + bankNonce + "||\nencrypted=" + encryptedBankNonce);
                System.out.println("||Sending ID: " + bankID + "||\nencrypted=" + encryptedBankID);

                //receive bank nonce and ATM nonce, both encrypted with bank private key
                String bankNonceToDecrypt = input.readLine();
                String atmNonceToDecrypt = input.readLine();

                String decryptedBankNonce = rsa.privateDecrypt(bankNonceToDecrypt, bankPrivateKey);
                String decryptedATMNonce = rsa.privateDecrypt(atmNonceToDecrypt, bankPrivateKey);

                System.out.println("\n||Received Bank Nonce: " + decryptedBankNonce + "||\nencrypted=" + bankNonceToDecrypt);
                System.out.println("||Received ATM Nonce: " + decryptedATMNonce + "||\nencrypted=" + atmNonceToDecrypt);


                //send ATM nonce, encrypted with public key of ATM
                String encryptedATMNonce = rsa.publicEncrypt(decryptedATMNonce, ATMPublicKey);

                output.println(encryptedATMNonce);
                System.out.println("\n||Sending Nonce: " + decryptedATMNonce + "||\nencrypted=" + encryptedATMNonce);


                //double encrypting master key with bankPrivateKey then ATMPublicKey
                String masterKeyString = Base64.getEncoder().encodeToString(masterKey.getEncoded());
                String innerEncrypt = rsa.privateEncrypt(masterKeyString, bankPrivateKey);
                //splitting encryption in half for further encryption (this is done so that the size of the string to encrypt is not too big)
                int middle = innerEncrypt.length() / 2;
                String[] half = {innerEncrypt.substring(0, middle), innerEncrypt.substring(middle)};
                //encrypting the first half
                String firstHalfEncrypt = rsa.publicEncrypt(half[0], ATMPublicKey);
                String secondHalfEncrypt = rsa.publicEncrypt(half[1], ATMPublicKey);

                //sending double encrypted symmetric key masterKey
                System.out.println("\n||Sending Master Key to authenticated ATM: " + masterKeyString + "||");
                output.println(firstHalfEncrypt);
                output.println(secondHalfEncrypt);
                output.println("stop");


                socket.close();
            }
            else{
                switch(ATMID){
                    case "deposit":
                        //read master key file for this thread to get the master key
                        ObjectInputStream masterinput = new ObjectInputStream(new FileInputStream("MasterKey.xx"));
                        SecretKey masterKey = (SecretKey) masterinput.readObject();
                        masterinput.close();


                        DES des = new DES(masterKey);
                        String user = input.readLine();
                        String encryptedMAC = input.readLine();
                        String decryptedMAC = des.decrypt(encryptedMAC);
                        System.out.println("decrypted data: " + decryptedMAC);
                        //result[0] contains deposit amount
                        //result[1] contains MAC for deposit amount
                        String[] result = decryptedMAC.split(" ");
                        MAC mac = new MAC();
                        String recreatedMac = mac.createMAC(result[0], masterKey);
                        System.out.println("recreated Mac: " + result[1] + "\nmatched MAC!");

                        //updating bankData.txt
                        String depositAmountString = result[0];
                        double depositAmount = Double.parseDouble(depositAmountString);
                        String depositAmountDString = String.format("%.2f" ,depositAmount);
                        System.out.println("\nuser wants to deposit $" + depositAmountDString);

                        Scanner scanner = new Scanner(new File("bankData.txt"));
                        StringBuffer buffer = new StringBuffer();
                        while(scanner.hasNextLine()){
                            String line = scanner.nextLine();
                            if(line.startsWith(user)){
                                String[] parts = line.split(" ");
                                Double balance = Double.parseDouble(parts[1]);
                                balance = balance + depositAmount;
                                String newBalance = String.format("%.2f", balance);
                                buffer.append(user + " " + newBalance+System.lineSeparator());
                            }
                            else {
                                buffer.append(line+System.lineSeparator());
                            }
                        }
                        String fileContents = buffer.toString();
                        System.out.println("Contents of the file: " + fileContents);
                        scanner.close();
                        FileWriter writer = new FileWriter("bankData.txt");
                        writer.append(fileContents);
                        writer.flush();


                        break;

                    case "withdrawal":
                        System.out.println("\nuser wants to withdrawal");
                        break;

                    case "inquiry":
                        System.out.println("\nuser wants to check balance");
                        break;

                    default:
                        System.out.println("\nUnexpected Behaviour");
                        break;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    //Method to save user balances to file
//    void saveBalances() {
//        //userBalanceMap.put(currentUser, balance);
//        try (PrintWriter writer = new PrintWriter(new FileWriter("bankData.txt"))) {
//            for (Map.Entry<String, Double> entry : userBalanceMap.entrySet()) {
//                writer.println(entry.getKey() + " " + entry.getValue());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(ClientATM_MainMenu.this, "Error saving user balances to file.");
//        }
//    }

    //Method to log user actions
    void logAction(String user, String action, double amount) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("audit_log.txt", true))) {
            //Get current time
            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = currentTime.format(formatter);

            //Write log entry to file
            writer.println(user + " " + action + " $" + String.format("%.2f", amount) + " " + formattedTime);
            output.println("success");
        } catch (IOException e) {
            e.printStackTrace();
            //If unable to write to file
            output.println("error");

            //show this on the main menu after receiving "error"
            //JOptionPane.showMessageDialog(ClientATM_MainMenu.this, "Error writing to audit log!");
        }
    }
}
