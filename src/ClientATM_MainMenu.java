import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientATM_MainMenu extends JFrame{

    private JPanel MainMenuPanel;
    private JLabel welcomeMessage;
    private JButton deposit;
    private JButton balanceInquiry;
    private JButton withdrawal;

    public ClientATM_MainMenu(String user){
        setTitle("Client ATM");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocationRelativeTo(null);
        setVisible(true);
        setContentPane(MainMenuPanel);
        ClientATM_MainMenu.this.welcomeMessage.setText("Welcome, " + user);

        //when user clicks deposit button
        deposit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        //when user clicks withdrawal button
        withdrawal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        //when user clicks balance inquiry button
        balanceInquiry.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    public static void main(String[] args){
        new ClientATM_MainMenu("test User");
    }

}
