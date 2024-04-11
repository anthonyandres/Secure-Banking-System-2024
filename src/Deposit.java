import javax.swing.*;

public class Deposit {
    private ClientATM_MainMenu mainMenu;
    private String user;

    public Deposit(ClientATM_MainMenu mainMenu, String user) {
        this.mainMenu = mainMenu;
        this.user = user;
    }

    public void handleDeposit() {
        //Asks user for deposit amount
        String depositAmountString = JOptionPane.showInputDialog(mainMenu, "Enter deposit amount:");
        try {
            double depositAmount = Double.parseDouble(depositAmountString);
            // Perform deposit operation
            deposit(depositAmount);
            // Log the deposit
            logAction(user, "deposit", depositAmount);
        } catch (NumberFormatException ex) {
            // If user enters invalid input
            JOptionPane.showMessageDialog(mainMenu, "Invalid amount. Please enter a valid number.");
        }
    }

    // Method to handle deposit operation
    private void deposit(double amount) {
        mainMenu.balance += amount; // Update balance
        mainMenu.saveBalances(); // Save balance to file
        JOptionPane.showMessageDialog(mainMenu, "Deposit of $" + String.format("%.2f", amount) + " successful. New balance: $" + String.format("%.2f", mainMenu.balance));
    }

    // Method to log user actions
    private void logAction(String user, String action, double amount) {
        mainMenu.logAction(user, action, amount);
    }
}
