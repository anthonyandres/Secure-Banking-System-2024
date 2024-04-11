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
    private double balance = 0;
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
                //Asks user for deposit amount
                String depositAmountString = JOptionPane.showInputDialog(ClientATM_MainMenu.this, "Enter deposit amount:");
                try {
                    double depositAmount = Double.parseDouble(depositAmountString);
                    //Perform deposit operation
                    deposit(depositAmount);
                    //Log the deposit
                    logAction(user, "deposit", depositAmount);
                } catch (NumberFormatException ex) {
                    //If user enters invalid input
                    JOptionPane.showMessageDialog(ClientATM_MainMenu.this, "Invalid amount. Please enter a valid number.");
                }
            }
        });

        //when user clicks withdrawal button
        withdrawal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Asks user for withdrawal amount
                String withdrawalAmountString = JOptionPane.showInputDialog(ClientATM_MainMenu.this, "Enter withdrawal amount:");
                try {
                    double withdrawalAmount = Double.parseDouble(withdrawalAmountString);
                    //Perform withdrawal operation
                    withdrawal(withdrawalAmount);
                    //Log the withdrawal
                    logAction(user, "withdrawal", withdrawalAmount);
                } catch (NumberFormatException ex) {
                    //If user enters invalid input
                    JOptionPane.showMessageDialog(ClientATM_MainMenu.this, "Invalid amount. Please enter a valid number.");
                }
            }
        });

        //when user clicks balance inquiry button
        balanceInquiry.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Show user's balance
                JOptionPane.showMessageDialog(ClientATM_MainMenu.this, "Your current balance is: $" + String.format("%.2f", balance));
                //Log the balance inquiry
                logAction(user, "balance inquiry", balance);
            }
        });
    }

    //Method to handle deposit operation
    private void deposit(double amount) {
        balance += amount; //Update balance
        saveBalances(); //Save balance to file
        JOptionPane.showMessageDialog(ClientATM_MainMenu.this, "Deposit of $" + String.format("%.2f", amount) + " successful. New balance: $" + String.format("%.2f", balance));
    }

    //Method to handle withdrawal operation
    private void withdrawal(double amount) {
        if (balance >= amount) {
            balance -= amount; //Update balance
            saveBalances(); //Save balance to file
            JOptionPane.showMessageDialog(ClientATM_MainMenu.this, "Withdrawal of $" + String.format("%.2f", amount) + " successful. New balance: $" + String.format("%.2f", balance));
        } else {
            JOptionPane.showMessageDialog(ClientATM_MainMenu.this, "Insufficient funds.");
        }
    }

    //Method to log user actions
    private void logAction(String user, String action, double amount) {
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
    private void saveBalances() {
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