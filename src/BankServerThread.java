import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

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

                        //logging into audit_log.txt
                        logAction(user, "deposit", depositAmount);
                        break;

                    case "withdrawal":
                        //read master key file for this thread to get the master key
                        ObjectInputStream masterinputwithdrawal = new ObjectInputStream(new FileInputStream("MasterKey.xx"));
                        SecretKey masterKeyWithdrawal = (SecretKey) masterinputwithdrawal.readObject();
                        masterinputwithdrawal.close();

                        String userWithdrawal = input.readLine();

                        //reading how much the user has and sending it to them
                        Scanner scannerWithdrawal = new Scanner(new File("bankData.txt"));
                        while(scannerWithdrawal.hasNextLine()){
                            String line = scannerWithdrawal.nextLine();
                            if(line.startsWith(userWithdrawal)){
                                String[] parts = line.split(" ");

                                //encrypting+MAC the balance of the user
                                DES desTMP = new DES(masterKeyWithdrawal);
                                MAC macTMP = new MAC();
                                String createdMacTMP = macTMP.createMAC(parts[1], masterKeyWithdrawal);
                                String balance = parts[1] + " " + createdMacTMP;
                                String encryptedBalance = desTMP.encrypt(balance);
                                output.println(encryptedBalance);
                                System.out.println(userWithdrawal + " has $" + String.format("%.2f" ,Double.parseDouble(parts[1])));
                                System.out.println("||sending secure data: " + balance + "||\nencrypted="+encryptedBalance);
                                break;
                            }
                        }
                        scannerWithdrawal.close();

                        //receiving the encrypted+MAC withdrawal amount from client ATM
                        DES desWithdrawal = new DES(masterKeyWithdrawal);
                        MAC macWithdrawal = new MAC();
                        String userWithdrawlToDecrypt = input.readLine();
                        if(userWithdrawlToDecrypt.equals("error")){
                            break;
                        }
                        String userWithdrawalMAC = desWithdrawal.decrypt(userWithdrawlToDecrypt);
                        System.out.println("decrypted data: " + userWithdrawalMAC);
                        //result[0] contains deposit amount
                        //result[1] contains MAC for deposit amount
                        String[] resultWithdrawal = userWithdrawalMAC.split(" ");
                        String recreatedMacWithdrawal = macWithdrawal.createMAC(resultWithdrawal[0], masterKeyWithdrawal);
                        System.out.println("recreated Mac: " + resultWithdrawal[1] + "\nmatched MAC!");
                        String withdrawalAmountString = resultWithdrawal[0];
                        Double withdrawalAmount = Double.parseDouble(withdrawalAmountString);


                        //updating bankData.txt
                        scannerWithdrawal = new Scanner(new File("bankData.txt"));
                        StringBuffer bufferWithdrawal = new StringBuffer();
                        while(scannerWithdrawal.hasNextLine()){
                            String line = scannerWithdrawal.nextLine();
                            if(line.startsWith(userWithdrawal)){
                                String[] parts = line.split(" ");
                                Double balance = Double.parseDouble(parts[1]);
                                balance = balance - withdrawalAmount;
                                String newBalance = String.format("%.2f", balance);
                                bufferWithdrawal.append(userWithdrawal + " " + newBalance+System.lineSeparator());
                            }
                            else {
                                bufferWithdrawal.append(line+System.lineSeparator());
                            }
                        }
                        String fileContentsWithdrawal = bufferWithdrawal.toString();
                        System.out.println("Contents of the file: " + fileContentsWithdrawal);
                        FileWriter writerWrtithdrawal = new FileWriter("bankData.txt");
                        writerWrtithdrawal.append(fileContentsWithdrawal);
                        writerWrtithdrawal.flush();

                        //logging into audit_log.txt
                        logAction(userWithdrawal, "withdrawal", withdrawalAmount);
                        break;

                    case "inquiry":
                        //read master key file for this thread to get the master key
                        ObjectInputStream masterinputinquiry = new ObjectInputStream(new FileInputStream("MasterKey.xx"));
                        SecretKey masterKeyInquiry = (SecretKey) masterinputinquiry.readObject();
                        masterinputinquiry.close();

                        String userInquiry = input.readLine();

                        //reading how much the user has and sending it to them
                        Scanner scannerInquiry = new Scanner(new File("bankData.txt"));
                        while(scannerInquiry.hasNextLine()){
                            String line = scannerInquiry.nextLine();
                            if(line.startsWith(userInquiry)){
                                String[] parts = line.split(" ");

                                //encrypting+MAC the balance of the user
                                DES desTMP = new DES(masterKeyInquiry);
                                MAC macTMP = new MAC();
                                String createdMacTMP = macTMP.createMAC(parts[1], masterKeyInquiry);
                                String balance = parts[1] + " " + createdMacTMP;
                                String encryptedBalance = desTMP.encrypt(balance);
                                output.println(encryptedBalance);
                                System.out.println(userInquiry + " has $" + String.format("%.2f" ,Double.parseDouble(parts[1])));
                                System.out.println("||sending secure data: " + balance + "||\nencrypted="+encryptedBalance);
                                break;
                            }
                        }
                        scannerInquiry.close();
                        break;

                    case "login":
                        //read master key file for this thread to get the master key
                        ObjectInputStream masterinputLR = new ObjectInputStream(new FileInputStream("MasterKey.xx"));
                        SecretKey masterKeyLR = (SecretKey) masterinputLR.readObject();
                        masterinputLR.close();

                        //receiving secure username and password
                        String userLRToDecrypt = input.readLine();
                        String passLRToDecrypt = input.readLine();

                        //decrypting and checking MAC
                        //receiving the encrypted+MAC withdrawal amount from client ATM
                        DES desLR = new DES(masterKeyLR);
                        MAC macLR = new MAC();

                        String usernameMAC = desLR.decrypt(userLRToDecrypt);
                        String passwordMAC = desLR.decrypt(passLRToDecrypt);
                        System.out.println("decrypted username: " + usernameMAC);
                        System.out.println("decrypted password: " + passwordMAC);
                        //result[0] contains deposit amount
                        //result[1] contains MAC for deposit amount
                        String[] userResult = usernameMAC.split(" ");
                        String[] passResult = passwordMAC.split(" ");
                        String recreatedMacUsername = macLR.createMAC(userResult[0], masterKeyLR);
                        String recreatedMacPassword = macLR.createMAC(passResult[0], masterKeyLR);
                        System.out.println("recreated Username Mac: " + userResult[1] + "\nmatched MAC!");
                        System.out.println("recreated Password Mac: " + passResult[1] + "\nmatched MAC!");
                        String username = userResult[0];
                        String password = passResult[0];

                        File users = new File("users.txt");
                        Scanner scannerLR;
                        try {
                            scannerLR = new Scanner(users);
                        } catch (FileNotFoundException ex) {
                            throw new RuntimeException(ex);
                        }

                        boolean invalid = true;
                        //loop through entire list of users
                        while (scannerLR.hasNextLine()) {
                            String userPass = scannerLR.nextLine();
                            String[] verify = userPass.split("\\s+");

                            //if username and password match, successful login
                            if(Objects.equals(verify[0], username) && Objects.equals(verify[1], password)){
                                //JOptionPane.showMessageDialog(ClientATM_Login.this, "Login Success!\nusername: " + username + "\npassword: " + password);
                                invalid = false;
                                String successMAC = macLR.createMAC("success", masterKeyLR);
                                String toEncryptSuccess = "success " + successMAC;
                                String encryptedSuccess = desLR.encrypt(toEncryptSuccess);
                                output.println(encryptedSuccess);
                                System.out.println("||Login success, letting ATM know securely: " + toEncryptSuccess + "||\nencrypted="+encryptedSuccess);
                                //ClientATM_Login.this.dispose();
                                //new ClientATM_MainMenu(username, masterKey);
                                break;
                            }
                        }
                        //if no successful login show new pane for invalid loin
                        if(invalid==true){
                            String successMAC = macLR.createMAC("failure", masterKeyLR);
                            String toEncryptFailure = "failure " + successMAC;
                            String encryptedFailure = desLR.encrypt(toEncryptFailure);
                            output.println(encryptedFailure);
                            System.out.println("||Login failed, letting ATM know securely: " + toEncryptFailure + "||\nencrypted="+encryptedFailure);
                            output.println(encryptedFailure);
                            //JOptionPane.showMessageDialog(ClientATM_Login.this, "Invalid Login!");
                        }
                        Double zero = 0.0;
                        logAction(username, "login", zero);
                        break;

                    case "register":
                        //read master key file for this thread to get the master key
                        ObjectInputStream masterinputRE = new ObjectInputStream(new FileInputStream("MasterKey.xx"));
                        SecretKey masterKeyRE = (SecretKey) masterinputRE.readObject();
                        masterinputRE.close();

                        //receiving secure username and password
                        String userREToDecrypt = input.readLine();
                        String passREToDecrypt = input.readLine();

                        //decrypting and checking MAC
                        //receiving the encrypted+MAC withdrawal amount from client ATM
                        DES desRE = new DES(masterKeyRE);
                        MAC macRE = new MAC();

                        String usernameMACRE = desRE.decrypt(userREToDecrypt);
                        String passwordMACRE = desRE.decrypt(passREToDecrypt);
                        System.out.println("decrypted username: " + usernameMACRE);
                        System.out.println("decrypted password: " + passwordMACRE);
                        //result[0] contains deposit amount
                        //result[1] contains MAC for deposit amount
                        String[] userResultRE = usernameMACRE.split(" ");
                        String[] passResultRE = passwordMACRE.split(" ");
                        String recreatedMacUsernameRE = macRE.createMAC(userResultRE[0], masterKeyRE);
                        String recreatedMacPasswordRE = macRE.createMAC(passResultRE[0], masterKeyRE);
                        System.out.println("recreated Username Mac: " + userResultRE[1] + "\nmatched MAC!");
                        System.out.println("recreated Password Mac: " + passResultRE[1] + "\nmatched MAC!");
                        String usernameRE = userResultRE[0];
                        String passwordRE = passResultRE[0];


                        // Validate if username already exists
                        if (isUsernameExists(usernameRE)) {
                            //JOptionPane.showMessageDialog(ClientATM_Login.this, "Username already exists!");
                            //securely sending failure
                            String successMAC = macRE.createMAC("failure", masterKeyRE);
                            String toEncryptFailure = "failure " + successMAC;
                            String encryptedFailure = desRE.encrypt(toEncryptFailure);
                            output.println(encryptedFailure);
                            System.out.println("||Registration failed, letting ATM know securely: " + toEncryptFailure + "||\nencrypted="+encryptedFailure);
                            output.println(encryptedFailure);
                        } else {
                            // If username is unique, append it to the users.txt file
                            try (FileWriter fw = new FileWriter("users.txt", true);
                                 BufferedWriter bw = new BufferedWriter(fw);
                                 PrintWriter out = new PrintWriter(bw)) {
                                out.print("\n"+usernameRE + " " + passwordRE);

                                //securely sending success
                                String successMAC = macRE.createMAC("success", masterKeyRE);
                                String toEncryptSuccess = "success " + successMAC;
                                String encryptedSuccess = desRE.encrypt(toEncryptSuccess);
                                output.println(encryptedSuccess);
                                System.out.println("||Registration success, letting ATM know securely: " + toEncryptSuccess + "||\nencrypted="+encryptedSuccess);
                                output.println(encryptedSuccess);
                                //JOptionPane.showMessageDialog(ClientATM_Login.this, "Registration successful!\nAccount created with $0.00.");
                            } catch (IOException ex) {
                                String errorMAC = macRE.createMAC("error", masterKeyRE);
                                String toEncryptError = "error " + errorMAC;
                                String encryptedError = desRE.encrypt(toEncryptError);
                                output.println(encryptedError);
                                System.out.println("||Login success, letting ATM know securely: " + toEncryptError + "||\nencrypted="+encryptedError);
                                output.println(encryptedError);
                                //JOptionPane.showMessageDialog(ClientATM_Login.this, "Error occurred while registering!");
                                ex.printStackTrace();
                            }

                            // After successful registration, add default bank data (0 dollars to user's account)
                            try (FileWriter fw = new FileWriter("bankData.txt", true);
                                 BufferedWriter bw = new BufferedWriter(fw);
                                 PrintWriter out = new PrintWriter(bw)) {
                                out.print(usernameRE + " 0");
                            } catch (IOException ex) {
//                                JOptionPane.showMessageDialog(ClientATM_Login.this, "Error occurred while creating account!");
                                ex.printStackTrace();
                            }

                        }
                        Double zeroRE = 0.0;
                        logAction(usernameRE, "registration", zeroRE);
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

    //Method to log user actions
    void logAction(String user, String action, double amount) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("audit_log.txt", true))) {
            //Get current time
            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = currentTime.format(formatter);

            //Write log entry to file
            writer.println(user + " " + action + " $" + String.format("%.2f", amount) + " " + formattedTime);
            //output.println("success");
        } catch (IOException e) {
            e.printStackTrace();
            //If unable to write to file
            //output.println("error");

            //show this on the main menu after receiving "error"
            //JOptionPane.showMessageDialog(ClientATM_MainMenu.this, "Error writing to audit log!");
        }
    }

    // Method to check if username already exists
    private boolean isUsernameExists(String username) {
        File users = new File("users.txt");
        try (Scanner scanner = new Scanner(users)) {
            while (scanner.hasNextLine()) {
                String userPass = scanner.nextLine();
                String[] verify = userPass.split("\\s+");
                if (Objects.equals(verify[0], username)) {
                    return true; // Username already exists
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false; // Username is unique
    }
}
