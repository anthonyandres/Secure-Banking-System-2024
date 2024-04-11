import javax.swing.*;

public class BalanceInquiry {
    private ClientATM_MainMenu mainMenu;
    private String user;
    private double balance;

    public BalanceInquiry(ClientATM_MainMenu mainMenu, String user, double balance) {
        this.mainMenu = mainMenu;
        this.user = user;
        this.balance = balance;
    }

    public void handleBalanceInquiry() {
        // Show user's balance
        JOptionPane.showMessageDialog(mainMenu, "Your current balance is: $" + String.format("%.2f", balance));
        // Log the balance inquiry
        logAction(user, "balance inquiry", balance);
    }

    // Method to log user actions
    private void logAction(String user, String action, double amount) {
        mainMenu.logAction(user, action, amount);
    }
}
