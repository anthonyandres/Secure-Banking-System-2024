import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Objects;
import java.util.Scanner;

public class ClientATM_Login extends JFrame{
    private JPanel LoginPanel;
    private JPasswordField Password;
    public JTextField Username;
    private JButton loginButton;
    private JButton registerButton;

    public ClientATM_Login() {

        setTitle("Client ATM Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(250, 150);
        setLocationRelativeTo(null);
        setVisible(true);
        setContentPane(LoginPanel);


        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = Username.getText();
                String password = Password.getText();

                File users = new File("users.txt");
                Scanner scanner;
                try {
                    scanner = new Scanner(users);
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                }

                boolean invalid = true;
                //loop throuh entire list of users
                while (scanner.hasNextLine()) {
                    String userPass = scanner.nextLine();
                    String[] verify = userPass.split("\\s+");

                    //if username and password match, successful login
                    if(Objects.equals(verify[0], username) && Objects.equals(verify[1], password)){
                        JOptionPane.showMessageDialog(ClientATM_Login.this, "Login Success!\nusername: " + username + "\npassword: " + password);
                        invalid = false;
                        ClientATM_Login.this.dispose();
                        //ClientATM_Login.this.setVisible(false);
                        new ClientATM_MainMenu(username);
                        break;
                    }
                }
                //if no successful loin show new pane for invalid loin
                if(invalid==true){
                    JOptionPane.showMessageDialog(ClientATM_Login.this, "Invalid Login!");
                }

            }
        });
        // Action listener for register button
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = Username.getText();
                String password = Password.getText();

                // Validate if username already exists
                if (isUsernameExists(username)) {
                    JOptionPane.showMessageDialog(ClientATM_Login.this, "Username already exists!");
                } else {
                    // If username is unique, append it to the users.txt file
                    try (FileWriter fw = new FileWriter("users.txt", true);
                         BufferedWriter bw = new BufferedWriter(fw);
                         PrintWriter out = new PrintWriter(bw)) {
                        out.print("\n"+username + " " + password);
                        JOptionPane.showMessageDialog(ClientATM_Login.this, "Registration successful!\nAccount created with $0.00.");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(ClientATM_Login.this, "Error occurred while registering!");
                        ex.printStackTrace();
                    }

                    // After successful registration, add default bank data (0 dollars to user's account)
                    try (FileWriter fw = new FileWriter("bankData.txt", true);
                         BufferedWriter bw = new BufferedWriter(fw);
                         PrintWriter out = new PrintWriter(bw)) {
                        out.print("\n"+username + " 0");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(ClientATM_Login.this, "Error occurred while creating account!");
                        ex.printStackTrace();
                    }

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



    public static void main(String[] args){
        //read users.txt for list of usernames and passwords
        //for the lazy: user1 pass1
        new ClientATM_Login();
    }
}
