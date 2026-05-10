package smartparking.model;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class AuthFrame extends JFrame {

    private final String userType;
    private final Color typeColor;

    private JTextField loginEmailField;
    private JTextField loginIdField;

    private JTextField regNameField, regEmailField, regIdField, regPlateField;
    private JTextField regExtraField;       // للـ visitor فقط (passport)
    private JComboBox<String> facultyCombo; // للـ student
    private JComboBox<String> deptCombo;    // ✅ للـ employee - قائمة الكليات/الأقسام

    static final String[][] FACULTIES = {
        {"EN", "Engineering",            "EN - Engineering"},
        {"HC", "Health Professions",     "HC - Health Professions"},
        {"ST", "Science & Technology",   "ST - Science & Technology"},
        {"DA", "Dawah & Islamic",        "DA - Dawah & Islamic Studies"},
        {"AR", "Arts & Humanities",      "AR - Arts & Humanities"},
        {"IT", "Information Technology", "IT - Information Technology"},
        {"EC", "Economics",              "EC - Economics"},
        {"LW", "Law",                    "LW - Law"},
    };

    // ✅ قائمة الأقسام للموظفين
    static final String[] DEPARTMENTS = {
        "Engineering",
        "Health Professions",
        "Science & Technology",
        "Dawah & Islamic Studies",
        "Arts & Humanities",
        "Information Technology",
        "Economics",
        "Law",
        "Administration",
        "Finance",
        "Human Resources",
        "Student Affairs",
        "Library",
        "IT Support",
        "Security",
        "Maintenance",
    };

    private JPanel cardContainer;
    private CardLayout cardLayout;

    public AuthFrame(String userType, Color typeColor) {
        this.userType  = userType;
        this.typeColor = typeColor;
        setTitle("Smart Parking - " + capitalize(userType));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 650);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIConstants.LIGHT_BG);

        JPanel header = new JPanel();
        header.setBackground(typeColor);
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel backBtn = new JLabel("← Back");
        backBtn.setFont(UIConstants.SMALL_FONT);
        backBtn.setForeground(new Color(255, 255, 255, 180));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose();
                new WelcomeFrame().setVisible(true);
            }
        });

        JLabel titleLabel = new JLabel(capitalize(userType) + " Portal");
        titleLabel.setFont(UIConstants.TITLE_FONT);
        titleLabel.setForeground(UIConstants.WHITE);

        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(backBtn);
        header.add(Box.createVerticalStrut(8));
        header.add(titleLabel);

        JPanel tabPanel = new JPanel(new GridLayout(1, 2));
        tabPanel.setBackground(typeColor.darker());
        JButton loginTab    = new JButton("Login");
        JButton registerTab = new JButton("Register");
        styleTab(loginTab, true);
        styleTab(registerTab, false);

        loginTab.addActionListener(e -> {
            cardLayout.show(cardContainer, "login");
            styleTab(loginTab, true);
            styleTab(registerTab, false);
        });
        registerTab.addActionListener(e -> {
            cardLayout.show(cardContainer, "register");
            styleTab(loginTab, false);
            styleTab(registerTab, true);
        });

        tabPanel.add(loginTab);
        if (!"admin".equals(userType)) {
            tabPanel.add(registerTab);
        } else {
            tabPanel.setLayout(new GridLayout(1, 1));
        }

        cardLayout    = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setBackground(UIConstants.LIGHT_BG);
        cardContainer.add(buildLoginPanel(),    "login");
        cardContainer.add(buildRegisterPanel(), "register");

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.add(header,   BorderLayout.CENTER);
        topSection.add(tabPanel, BorderLayout.SOUTH);

        mainPanel.add(topSection,    BorderLayout.NORTH);
        mainPanel.add(cardContainer, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private void styleTab(JButton btn, boolean active) {
        btn.setFont(UIConstants.BUTTON_FONT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBackground(active ? UIConstants.WHITE : typeColor.darker());
        btn.setForeground(active ? typeColor : UIConstants.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 40));
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIConstants.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JLabel title = new JLabel("Welcome back!");
        title.setFont(UIConstants.SUBTITLE_FONT);
        title.setForeground(UIConstants.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginEmailField = UIConstants.createTextField();
        loginIdField    = UIConstants.createTextField();

        JButton loginBtn = UIConstants.createButton("Login", typeColor);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> doLogin());

        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        panel.add(makeFieldRow("Email", loginEmailField));
        panel.add(Box.createVerticalStrut(12));
        panel.add(makeFieldRow("ID Number", loginIdField));
        panel.add(Box.createVerticalStrut(25));
        panel.add(loginBtn);

        return panel;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIConstants.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JLabel title = new JLabel("Create Account");
        title.setFont(UIConstants.SUBTITLE_FONT);
        title.setForeground(UIConstants.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        regNameField  = UIConstants.createTextField();
        regEmailField = UIConstants.createTextField();

        String idLabel = switch (userType) {
            case "student"  -> "University ID Number";
            case "employee" -> "Employee Number";
            case "visitor"  -> "National/Passport ID";
            default         -> "ID Number";
        };
        regIdField = UIConstants.createTextField();

        // ✅ قائمة الكليات للطالب
        facultyCombo = new JComboBox<>();
        facultyCombo.setFont(UIConstants.BODY_FONT);
        facultyCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        for (String[] f : FACULTIES) facultyCombo.addItem(f[2]);

        JLabel badgePreview = new JLabel();
        badgePreview.setFont(new Font("Segoe UI", Font.BOLD, 13));
        badgePreview.setForeground(UIConstants.PRIMARY_BLUE);
        updateBadgePreview(badgePreview, 0);
        facultyCombo.addActionListener(e -> updateBadgePreview(badgePreview, facultyCombo.getSelectedIndex()));

        // ✅ قائمة الأقسام للموظف
        deptCombo = new JComboBox<>();
        deptCombo.setFont(UIConstants.BODY_FONT);
        deptCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        for (String dept : DEPARTMENTS) deptCombo.addItem(dept);

        // ✅ للـ visitor: حقل رقم الجواز
        regExtraField = UIConstants.createTextField();

        regPlateField = UIConstants.createTextField();

        JButton registerBtn = UIConstants.createButton("Register", typeColor);
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> doRegister());

        panel.add(title);
        panel.add(Box.createVerticalStrut(15));
        panel.add(makeFieldRow("Full Name", regNameField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(makeFieldRow("Email", regEmailField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(makeFieldRow(idLabel, regIdField));

        if (!userType.equals("admin")) {
            panel.add(Box.createVerticalStrut(10));

            if (userType.equals("student")) {
                // ✅ قائمة كليات الطالب
                JPanel facRow = new JPanel(new BorderLayout(0, 4));
                facRow.setBackground(UIConstants.WHITE);
                facRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
                JLabel facLabel = new JLabel("Faculty");
                facLabel.setFont(UIConstants.SMALL_FONT);
                facLabel.setForeground(UIConstants.TEXT_DARK);
                JPanel comboRow = new JPanel(new BorderLayout(8, 0));
                comboRow.setBackground(UIConstants.WHITE);
                comboRow.add(facultyCombo, BorderLayout.CENTER);
                comboRow.add(badgePreview, BorderLayout.EAST);
                facRow.add(facLabel,  BorderLayout.NORTH);
                facRow.add(comboRow,  BorderLayout.CENTER);
                panel.add(facRow);

            } else if (userType.equals("employee")) {
                // ✅ قائمة أقسام الموظف منسدلة
                JPanel deptRow = new JPanel(new BorderLayout(0, 4));
                deptRow.setBackground(UIConstants.WHITE);
                deptRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
                JLabel deptLabel = new JLabel("Department");
                deptLabel.setFont(UIConstants.SMALL_FONT);
                deptLabel.setForeground(UIConstants.TEXT_DARK);
                deptRow.add(deptLabel, BorderLayout.NORTH);
                deptRow.add(deptCombo, BorderLayout.CENTER);
                panel.add(deptRow);

            } else if (userType.equals("visitor")) {
                // ✅ حقل رقم الجواز للزائر
                panel.add(makeFieldRow("Passport Number", regExtraField));
            }

            panel.add(Box.createVerticalStrut(10));
            panel.add(makeFieldRow("License Plate", regPlateField));
        }

        panel.add(Box.createVerticalStrut(20));
        panel.add(registerBtn);
        return panel;
    }

    private JPanel makeFieldRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setBackground(UIConstants.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        JLabel label = new JLabel(labelText);
        label.setFont(UIConstants.SMALL_FONT);
        label.setForeground(UIConstants.TEXT_DARK);
        row.add(label, BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private void doLogin() {
        String email    = loginEmailField.getText().trim();
        String idNumber = loginIdField.getText().trim();

        if (email.isEmpty() || idNumber.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        if ("admin".equals(userType)) {
            if (!email.equals("wafaaali712004@gmail.com") || !idNumber.equals("22310713")) {
                showError("Invalid admin credentials.");
                return;
            }
            try {
                User user = Database.login(email, idNumber);
                if (user == null) {
                    showError("Admin not found in database.");
                    return;
                }
                JOptionPane.showMessageDialog(this,
                    "Welcome, " + user.getUserName() + "!",
                    "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                openDashboard(user);
            } catch (SQLException e) {
                showError("Database error: " + e.getMessage());
            }
            return;
        }

        try {
            User user = Database.login(email, idNumber);
            if (user == null) {
                showError("Invalid email or ID number.");
                return;
            }
            if (!user.getUserType().equals(userType)) {
                showError("This account is not a " + capitalize(userType) + " account.");
                return;
            }
            JOptionPane.showMessageDialog(this,
                "Welcome back, " + user.getUserName() + "!",
                "Login Successful", JOptionPane.INFORMATION_MESSAGE);
            openDashboard(user);
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private void doRegister() {
        String name     = regNameField.getText().trim();
        String email    = regEmailField.getText().trim();
        String idNumber = regIdField.getText().trim();
        String extra;

        if (userType.equals("student")) {
            int idx = facultyCombo.getSelectedIndex();
            extra = (idx >= 0) ? FACULTIES[idx][0] + " - " + FACULTIES[idx][1] : "";
        } else if (userType.equals("employee")) {
            // ✅ القسم من القائمة المنسدلة
            extra = (String) deptCombo.getSelectedItem();
            if (extra == null) extra = "";
        } else {
            extra = regExtraField != null ? regExtraField.getText().trim() : "";
        }

        String plate = regPlateField != null ? regPlateField.getText().trim().toUpperCase() : "";

        if (!Validator.isValidName(name)) {
            showError("Name must be at least 3 characters (letters only)."); return;
        }
        if (!Validator.isValidEmail(email)) {
            showError("Please enter a valid email address."); return;
        }
        if (!Validator.isValidIdNumber(idNumber)) {
            showError("ID number must be exactly 8 digits."); return;
        }
        if (!userType.equals("admin") && !Validator.isValidLicensePlate(plate)) {
            showError("Please enter a valid license plate (5-8 uppercase letters/numbers/dashes)."); return;
        }

        try {
            if (Database.emailExists(email)) {
                showError("This email is already registered."); return;
            }
            if (Database.idNumberExists(idNumber)) {
                showError("This ID number is already registered."); return;
            }

            User user = new User();
            user.setUserName(name);
            user.setEmail(email);
            user.setIdNumber(idNumber);
            user.setUserType(userType);

            switch (userType) {
                case "student"  -> user.setFaculty(extra);
                case "employee" -> user.setDepartment(extra);
                case "visitor"  -> user.setPassportId(extra);
            }

            Database.register(user);

            if (!userType.equals("admin") && !plate.isEmpty()) {
                Database.addVehicle(plate, user.getUserId());
            }

            JOptionPane.showMessageDialog(this,
                "Account created successfully! Please login.",
                "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(cardContainer, "login");

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private void openDashboard(User user) {
        dispose();
        if ("admin".equals(userType)) {
            new AdminDashboard(user).setVisible(true);
        } else {
            new UserDashboard(user, typeColor).setVisible(true);
        }
    }

    private void updateBadgePreview(JLabel badge, int idx) {
        if (idx < 0 || idx >= FACULTIES.length) return;
        badge.setText("[" + FACULTIES[idx][0] + "]");
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}