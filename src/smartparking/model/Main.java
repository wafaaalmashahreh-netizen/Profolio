package smartparking.model;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseInitializer.initialize();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Failed to connect to database.\n\nMake sure:\n" +
                "1. XAMPP is running\n" +
                "2. MySQL service is started\n" +
                "3. Database 'smart_parking' exists\n\nError: " + e.getMessage(),
                "Database Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new WelcomeFrame().setVisible(true);
        });
    }
}