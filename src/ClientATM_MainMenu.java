import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ClientATM_MainMenu extends JFrame{

    private JPanel MainMenuPanel;
    private JLabel welcomeMessage;
    private JButton deposit;
    private JButton balanceInquiry;
    private JButton withdrawal;
    private JLabel tmp;
    double balance = 0;
    private String currentUser;
    private Map<String, Double> userBalanceMap = new HashMap<>();

    public ClientATM_MainMenu(String user){
        this.currentUser = user;
        setTitle("Client ATM");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocationRelativeTo(null);
        setVisible(true);
        setContentPane(MainMenuPanel);
        ClientATM_MainMenu.this.welcomeMessage.setText("Welcome, " + user);

        //Load user balances from file
        loadBalances();

        //Set user's balance
        if (userBalanceMap.containsKey(user)) {
            balance = userBalanceMap.get(user);
        }

        //when user clicks deposit button
        deposit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Call deposit handler
                new Deposit(ClientATM_MainMenu.this, currentUser).handleDeposit();
            }
        });

        //when user clicks withdrawal button
        withdrawal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Call withdrawal handler
                new Withdrawal(ClientATM_MainMenu.this, currentUser).handleWithdrawal();
            }
        });

        //when user clicks balance inquiry button
        balanceInquiry.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Call balance inquiry handler
                new BalanceInquiry(ClientATM_MainMenu.this, currentUser, balance).handleBalanceInquiry();
            }
        });
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
        } catch (IOException e) {
            e.printStackTrace();
            //If unable to write to file
            JOptionPane.showMessageDialog(ClientATM_MainMenu.this, "Error writing to audit log!");
        }
    }

    //Method to load user balances from file
    private void loadBalances() {
        try (BufferedReader reader = new BufferedReader(new FileReader("bankData.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    String userID = parts[0];
                    double balance = Double.parseDouble(parts[1]);
                    userBalanceMap.put(userID, balance);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(ClientATM_MainMenu.this, "Error reading user balances from file.");
        }
    }

    //Method to save user balances to file
    void saveBalances() {
        userBalanceMap.put(currentUser, balance);
        try (PrintWriter writer = new PrintWriter(new FileWriter("bankData.txt"))) {
            for (Map.Entry<String, Double> entry : userBalanceMap.entrySet()) {
                writer.println(entry.getKey() + " " + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(ClientATM_MainMenu.this, "Error saving user balances to file.");
        }
    }

    public static void main(String[] args){
        // Don't run this by itself, this is just for testing sake
        // Run ClientATM_Login if you want to see the full process of logging in
        new ClientATM_MainMenu("test User");
    }
}