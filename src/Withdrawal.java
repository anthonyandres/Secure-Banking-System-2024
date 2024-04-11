import javax.swing.*;

public class Withdrawal {
    private ClientATM_MainMenu mainMenu;
    private String user;

    public Withdrawal(ClientATM_MainMenu mainMenu, String user) {
        this.mainMenu = mainMenu;
        this.user = user;
    }

    public void handleWithdrawal() {
        // Asks user for withdrawal amount
        String withdrawalAmountString = JOptionPane.showInputDialog(mainMenu, "Enter withdrawal amount:");
        try {
            double withdrawalAmount = Double.parseDouble(withdrawalAmountString);
            // Perform withdrawal operation
            withdrawal(withdrawalAmount);
            // Log the withdrawal
            logAction(user, "withdrawal", withdrawalAmount);
        } catch (NumberFormatException ex) {
            // If user enters invalid input
            JOptionPane.showMessageDialog(mainMenu, "Invalid amount. Please enter a valid number.");
        }
    }

    // Method to handle withdrawal operation
    private void withdrawal(double amount) {
        if (mainMenu.balance >= amount) {
            mainMenu.balance -= amount; // Update balance
            mainMenu.saveBalances(); // Save balance to file
            JOptionPane.showMessageDialog(mainMenu, "Withdrawal of $" + String.format("%.2f", amount) + " successful. New balance: $" + String.format("%.2f", mainMenu.balance));
        } else {
            JOptionPane.showMessageDialog(mainMenu, "Insufficient funds.");
        }
    }

    // Method to log user actions
    private void logAction(String user, String action, double amount) {
        mainMenu.logAction(user, action, amount);
    }
}
