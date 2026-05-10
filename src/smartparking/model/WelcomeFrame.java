package smartparking.model;

import javax.swing.*;
import java.awt.*;

public class WelcomeFrame extends JFrame {

    public WelcomeFrame() {
        setTitle("Smart Parking System - University");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIConstants.DARK_BLUE);

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(UIConstants.DARK_BLUE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));

        JLabel titleLabel = new JLabel("🅿 Smart Parking System");
        titleLabel.setFont(UIConstants.TITLE_FONT);
        titleLabel.setForeground(UIConstants.WHITE);

        JLabel subtitleLabel = new JLabel("University Campus Parking Management");
        subtitleLabel.setFont(UIConstants.BODY_FONT);
        subtitleLabel.setForeground(new Color(189, 195, 199));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(UIConstants.DARK_BLUE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(8));
        titlePanel.add(subtitleLabel);
        headerPanel.add(titlePanel);

        // Card panel — 3 كاردات بس
        JPanel cardPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        cardPanel.setBackground(UIConstants.DARK_BLUE);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        cardPanel.add(createTypeCard("🎓", "Student", "University ID", UIConstants.STUDENT_COLOR, "student"));
        cardPanel.add(createTypeCard("👔", "Employee", "Employee Number", UIConstants.EMPLOYEE_COLOR, "employee"));
        cardPanel.add(createTypeCard("👤", "Visitor", "National/Passport ID", UIConstants.VISITOR_COLOR, "visitor"));

        // Bottom panel — hint + زر الادمن الصغير
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        bottomPanel.setBackground(UIConstants.DARK_BLUE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        JLabel loginHint = new JLabel("Already have an account? Click your type above to login.");
        loginHint.setFont(UIConstants.SMALL_FONT);
        loginHint.setForeground(new Color(149, 165, 166));

        JButton adminBtn = new JButton("🔑 Admin");
        adminBtn.setFont(UIConstants.SMALL_FONT);
        adminBtn.setBackground(UIConstants.DARK_BLUE);
        adminBtn.setForeground(new Color(149, 165, 166));
        adminBtn.setFocusPainted(false);
        adminBtn.setBorderPainted(false);
        adminBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        adminBtn.addActionListener(e -> openAuthFrame("admin", UIConstants.ADMIN_COLOR));

        bottomPanel.add(loginHint);
        bottomPanel.add(adminBtn);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createTypeCard(String emoji, String title, String subtitle, Color color, String type) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(25, 15, 25, 15));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel emojiLabel = new JLabel(emoji, SwingConstants.CENTER);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        emojiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(UIConstants.SUBTITLE_FONT);
        titleLabel.setForeground(UIConstants.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("<html><center>" + subtitle + "</center></html>", SwingConstants.CENTER);
        subLabel.setFont(UIConstants.SMALL_FONT);
        subLabel.setForeground(new Color(255, 255, 255, 180));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btn = UIConstants.createButton("Select", color.darker());
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> openAuthFrame(type, color));

        card.add(emojiLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(subLabel);
        card.add(Box.createVerticalStrut(15));
        card.add(btn);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(color.darker());
                card.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(color);
                card.repaint();
            }
        });

        return card;
    }

    private void openAuthFrame(String userType, Color color) {
        dispose();
        new AuthFrame(userType, color).setVisible(true);
    }
}