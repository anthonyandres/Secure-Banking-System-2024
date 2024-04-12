import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Scanner;

public class ClientATM_Login extends JFrame{
    private JPanel LoginPanel;
    private JPasswordField Password;
    public JTextField Username;
    private JButton loginButton;
    private JButton registerButton;
    private SecretKey masterKey;

    public ClientATM_Login(SecretKey masterKey) throws IOException {
        this.masterKey = masterKey;

        setTitle("Client ATM Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(250, 150);
        setLocationRelativeTo(null);
        setVisible(true);
        setContentPane(LoginPanel);

        File users = new File("users.txt");
        if(users.createNewFile()){
            System.out.println("File created: " + users.getName());
        }
        else{
            System.out.println("accessing users");
        }

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = Username.getText();
                String password = Password.getText();
                String hostName = "localhost";
                int portNumber = 4444;

                try(
                        Socket kkSocket = new Socket(hostName, portNumber);
                        PrintWriter output= new PrintWriter(kkSocket.getOutputStream(), true);
                        BufferedReader input = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
                ){
                    output.println("login");

                    DES desLR = new DES(masterKey);
                    MAC macLR = new MAC();

                    //Creating MAC and appending to username and password for encryption
                    String usernameMAC = macLR.createMAC(username, masterKey);
                    String passwordMAC = macLR.createMAC(password, masterKey);
                    String usernameToEncrypt = username + " " + usernameMAC;
                    String paswordToEncrypt = password + " " + passwordMAC;

                    //encrypting username and password + MAC
                    String encryptedMACUsername = desLR.encrypt(usernameToEncrypt);
                    String encryptedMACpassword = desLR.encrypt(paswordToEncrypt);

                    //sending securely to bank server
                    output.println(encryptedMACUsername);
                    output.println(encryptedMACpassword);
                    System.out.println("||Sending login information securely: " + usernameMAC + " " + passwordMAC + "||\nencrypted="+encryptedMACUsername + "\nencrypted="+encryptedMACpassword);

                    //receiving result securely
                    String toDecryptResult = input.readLine();
                    String decryptedResultMAC = desLR.decrypt(toDecryptResult);
                    System.out.println("decrypted data: " + decryptedResultMAC);
                    //result[0] contains deposit amount
                    //result[1] contains MAC for deposit amount
                    String[] resultResult = decryptedResultMAC.split(" ");
                    String recreatedMacBalance = macLR.createMAC(resultResult[0], masterKey);
                    System.out.println("recreated Mac: " + resultResult[1] + "\nmatched MAC!");
                    String resultString = resultResult[0];

                    if(resultString.equals("success")){
                        JOptionPane.showMessageDialog(ClientATM_Login.this, "Login Success!\nusername: " + username + "\npassword: " + password);
                        ClientATM_Login.this.dispose();
                        new ClientATM_MainMenu(username, masterKey);
                    }
                    else if (resultString.equals("failure")) {
                        JOptionPane.showMessageDialog(ClientATM_Login.this, "Invalid Login!");
                    }


                } catch (UnknownHostException e2) {
                    System.err.println("Don't know about host " + hostName);
                    System.exit(1);
                } catch (IOException e2) {
                    System.err.println("Couldn't get I/O for the connection to " +
                            hostName);
                    System.exit(1);
                } catch (Exception e2) {
                    throw new RuntimeException(e2);
                }
            }
        });
        // Action listener for register button
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = Username.getText();
                String password = Password.getText();
                String hostName = "localhost";
                int portNumber = 4444;

                try(
                        Socket kkSocket = new Socket(hostName, portNumber);
                        PrintWriter output= new PrintWriter(kkSocket.getOutputStream(), true);
                        BufferedReader input = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
                ){
                    output.println("register");

                    DES desLR = new DES(masterKey);
                    MAC macLR = new MAC();

                    //Creating MAC and appending to username and password for encryption
                    String usernameMAC = macLR.createMAC(username, masterKey);
                    String passwordMAC = macLR.createMAC(password, masterKey);
                    String usernameToEncrypt = username + " " + usernameMAC;
                    String paswordToEncrypt = password + " " + passwordMAC;

                    //encrypting username and password + MAC
                    String encryptedMACUsername = desLR.encrypt(usernameToEncrypt);
                    String encryptedMACpassword = desLR.encrypt(paswordToEncrypt);

                    //sending securely to bank server
                    output.println(encryptedMACUsername);
                    output.println(encryptedMACpassword);
                    System.out.println("||Sending register information securely: " + usernameMAC + " " + passwordMAC + "||\nencrypted="+encryptedMACUsername + "\nencrypted="+encryptedMACpassword);

                    //receiving result securely
                    String toDecryptResult = input.readLine();
                    String decryptedResultMAC = desLR.decrypt(toDecryptResult);
                    System.out.println("decrypted data: " + decryptedResultMAC);
                    //result[0] contains deposit amount
                    //result[1] contains MAC for deposit amount
                    String[] resultResult = decryptedResultMAC.split(" ");
                    String recreatedMacBalance = macLR.createMAC(resultResult[0], masterKey);
                    System.out.println("recreated Mac: " + resultResult[1] + "\nmatched MAC!");
                    String resultString = resultResult[0];

                    if(resultString.equals("success")){
                        JOptionPane.showMessageDialog(ClientATM_Login.this, "Registration successful!\nAccount created with $0.00.");
                        ClientATM_Login.this.dispose();
                        new ClientATM_MainMenu(username, masterKey);
                    }
                    else if (resultString.equals("failure")) {
                        JOptionPane.showMessageDialog(ClientATM_Login.this, "Username already exists!");
                    }
                    else if(resultString.equals("error")){
                        JOptionPane.showMessageDialog(ClientATM_Login.this, "Error occurred while registering!");
                    }


                } catch (UnknownHostException e2) {
                    System.err.println("Don't know about host " + hostName);
                    System.exit(1);
                } catch (IOException e2) {
                    System.err.println("Couldn't get I/O for the connection to " +
                            hostName);
                    System.exit(1);
                } catch (Exception e2) {
                    throw new RuntimeException(e2);
                }
            }
        });
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



    public static void main(String[] args) throws IOException {
        //read users.txt for list of usernames and passwords
        //for the lazy: user1 pass1
        //new ClientATM_Login();
    }
}
