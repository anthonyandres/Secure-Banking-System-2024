import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.Scanner;

public class ClientATM_Login extends JFrame{
    private JPanel LoginPanel;
    private JPasswordField Password;
    public JTextField Username;
    private JButton loginButton;

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
    }

    public static void main(String[] args){
        //read users.txt for list of usernames and passwords
        //for the lazy: user1 pass1
        new ClientATM_Login();
    }
}
