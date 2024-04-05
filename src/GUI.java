import javax.swing.*;
import java.awt.*;

public class GUI {

    public GUI(){
        JFrame frame = new JFrame();

        JButton button = new JButton("button");
        JLabel labelUsername = new JLabel("username:");
        JLabel labelPassword = new JLabel("password:");
        labelUsername.setBounds(10, 20, 80, 25);
        labelPassword.setBounds(10, 50, 80, 25);

        JTextField usernameText = new JTextField(20);
        usernameText.setBounds(100, 20, 265, 25);
        JTextField passwordText = new JTextField(20);
        passwordText.setBounds(100, 50, 265, 25);

        JButton loginButton = new JButton("login");
        loginButton.setBounds(100, 80, 80, 25);

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        panel.setLayout(null);
        panel.add(button);
        panel.add(labelUsername);
        panel.add(usernameText);
        panel.add(labelPassword);
        panel.add(passwordText);
        panel.add(loginButton);

        frame.setSize(700, 400);
        frame.add(panel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Login Window");
        frame.setVisible(true);

    }

    //just for testing
    public static void main(String[] args){
        new GUI();
    }
}
