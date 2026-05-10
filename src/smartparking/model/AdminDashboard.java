package smartparking.model;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class AdminDashboard extends JFrame {

    private final User adminUser;
    private JPanel contentPanel;
    private CardLayout contentLayout;
    private Timer refreshTimer;

    public AdminDashboard(User admin) {
        this.adminUser = admin;
        setTitle("Smart Parking - Admin Panel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        initUI();
        showPanel("overview");
        startAutoRefresh();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIConstants.LIGHT_BG);
        mainPanel.add(buildTopBar(),  BorderLayout.NORTH);
        mainPanel.add(buildSideNav(), BorderLayout.WEST);

        contentLayout = new CardLayout();
        contentPanel  = new JPanel(contentLayout);
        contentPanel.setBackground(UIConstants.LIGHT_BG);

        // بنبني الـ panels مرة أولى - showPanel بتعيد بناءها عند كل ضغطة
        contentPanel.add(buildOverviewPanel(),     "overview");
        contentPanel.add(buildReservationsPanel(), "reservations");
        contentPanel.add(buildSpotsPanel(),        "spots");
        contentPanel.add(buildUsersPanel(),        "users");

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    // ===== TOP BAR =====
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIConstants.ADMIN_COLOR);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel title = new JLabel("Smart Parking — Admin Panel");
        title.setFont(UIConstants.SUBTITLE_FONT);
        title.setForeground(UIConstants.WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(UIConstants.ADMIN_COLOR);

        JLabel userLabel = new JLabel("Admin: " + adminUser.getUserName());
        userLabel.setFont(UIConstants.SMALL_FONT);
        userLabel.setForeground(UIConstants.WHITE);

        JButton logoutBtn = UIConstants.createButton("Logout", UIConstants.TEXT_DARK);
        logoutBtn.addActionListener(e -> {
            if (refreshTimer != null) refreshTimer.stop();
            dispose();
            new WelcomeFrame().setVisible(true);
        });

        right.add(userLabel);
        right.add(logoutBtn);
        bar.add(title, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ===== SIDE NAV =====
    private JPanel buildSideNav() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(new Color(44, 62, 80));
        nav.setPreferredSize(new Dimension(200, 0));
        nav.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        String[][] navItems = {
            {"Overview",         "overview"},
            {"All Reservations", "reservations"},
            {"Parking Spots",    "spots"},
            {"Manage Users",     "users"},
        };

        for (String[] item : navItems) {
            JButton btn = new JButton(item[0]);
            btn.setFont(UIConstants.BODY_FONT);
            btn.setForeground(UIConstants.WHITE);
            btn.setBackground(new Color(44, 62, 80));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
            String panelName = item[1];
            // ✅ FIX: كل ضغطة على الـ nav تبني الـ panel من جديد = بيانات محدّثة
            btn.addActionListener(e -> showPanel(panelName));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(new Color(52, 73, 94)); }
                public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(new Color(44, 62, 80)); }
            });
            nav.add(btn);
        }
        return nav;
    }

    // ===== OVERVIEW =====
    private JPanel buildOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIConstants.LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("System Overview");
        title.setFont(UIConstants.TITLE_FONT);
        title.setForeground(UIConstants.TEXT_DARK);

        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        statsPanel.setBackground(UIConstants.LIGHT_BG);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        try {
            List<ParkingSpot> spots = Database.getAllSpots();
            long available = spots.stream().filter(ParkingSpot::isAvailable).count();
            long reserved  = spots.size() - available;

            List<Reservation> allRes = Database.getAllReservations();
            long activeRes = allRes.stream()
                .filter(r -> "confirmed".equals(r.getStatus()) || "pending".equals(r.getStatus()))
                .count();
            long totalRes = allRes.size();

            List<User> users = Database.getAllUsers();

            statsPanel.add(makeStatCard("Total Spots",         String.valueOf(spots.size()), new Color(52, 152, 219)));
            statsPanel.add(makeStatCard("Available Spots",     String.valueOf(available),    UIConstants.GREEN_SPOT));
            statsPanel.add(makeStatCard("Reserved Spots",      String.valueOf(reserved),     UIConstants.RED_SPOT));
            statsPanel.add(makeStatCard("Active Reservations", String.valueOf(activeRes),    new Color(155, 89, 182)));
            statsPanel.add(makeStatCard("Total Reservations",  String.valueOf(totalRes),     new Color(52, 152, 219)));
            statsPanel.add(makeStatCard("Registered Users",    String.valueOf(users.size()), new Color(230, 126, 34)));

        } catch (SQLException e) {
            statsPanel.add(new JLabel("Error loading stats: " + e.getMessage()));
        }

        panel.add(title,      BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel makeStatCard(String label, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UIConstants.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        JLabel valLabel = new JLabel(value);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valLabel.setForeground(color);
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(UIConstants.BODY_FONT);
        lblLabel.setForeground(UIConstants.TEXT_DARK);
        card.add(valLabel, BorderLayout.CENTER);
        card.add(lblLabel, BorderLayout.SOUTH);
        return card;
    }

    // ===== RESERVATIONS =====
    private JPanel buildReservationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIConstants.LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("All Reservations");
        title.setFont(UIConstants.SUBTITLE_FONT);
        title.setForeground(UIConstants.TEXT_DARK);

        String[] cols = {"Res ID", "User Name", "ID Number", "Vehicle", "Spot", "Area", "Date", "Start", "End", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        try {
            for (Reservation r : Database.getAllReservations()) {
                model.addRow(new Object[]{
                    r.getReservationId(), r.getUserName(), r.getUserId(),
                    r.getLicensePlate(), r.getSpotId(), r.getAreaName(),
                    r.getResDate(), r.getStartTime(), r.getEndTime(),
                    r.getStatus().toUpperCase()
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }

        JTable table = buildStyledTable(model);

        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String status = (String) model.getValueAt(row, 9);
                if (!sel) {
                    if (status.equals("CONFIRMED"))       c.setBackground(new Color(212, 237, 218));
                    else if (status.equals("PENDING"))    c.setBackground(new Color(255, 243, 205));
                    else if (status.equals("CANCELLED"))  c.setBackground(new Color(248, 215, 218));
                    else if (status.equals("EXPIRED"))    c.setBackground(new Color(222, 226, 230));
                    else if (status.equals("COMPLETED"))  c.setBackground(new Color(200, 220, 255));
                    else c.setBackground(Color.WHITE);
                }
                return c;
            }
        });

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(UIConstants.LIGHT_BG);
        filterPanel.add(new JLabel("Filter by status:"));

        JComboBox<String> statusFilter = new JComboBox<>(new String[]{
            "All", "PENDING", "CONFIRMED", "CANCELLED", "EXPIRED", "COMPLETED"
        });
        statusFilter.setFont(UIConstants.SMALL_FONT);
        statusFilter.addActionListener(e -> {
            String selected = (String) statusFilter.getSelectedItem();
            model.setRowCount(0);
            try {
                for (Reservation r : Database.getAllReservations()) {
                    String st = r.getStatus().toUpperCase();
                    if ("All".equals(selected) || st.equals(selected)) {
                        model.addRow(new Object[]{
                            r.getReservationId(), r.getUserName(), r.getUserId(),
                            r.getLicensePlate(), r.getSpotId(), r.getAreaName(),
                            r.getResDate(), r.getStartTime(), r.getEndTime(), st
                        });
                    }
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        JButton refreshBtn = UIConstants.createButton("Refresh", UIConstants.PRIMARY_BLUE);
        refreshBtn.addActionListener(e -> showPanel("reservations"));

        filterPanel.add(statusFilter);
        filterPanel.add(refreshBtn);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 3));
        legend.setBackground(UIConstants.LIGHT_BG);
        legend.add(makeLegend(new Color(212, 237, 218), "Confirmed"));
        legend.add(makeLegend(new Color(255, 243, 205), "Pending"));
        legend.add(makeLegend(new Color(248, 215, 218), "Cancelled"));
        legend.add(makeLegend(new Color(222, 226, 230), "Expired"));
        legend.add(makeLegend(new Color(200, 220, 255), "Completed"));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(UIConstants.LIGHT_BG);
        northPanel.add(title,       BorderLayout.NORTH);
        northPanel.add(filterPanel, BorderLayout.CENTER);
        northPanel.add(legend,      BorderLayout.SOUTH);

        panel.add(northPanel,             BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel makeLegend(Color color, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setBackground(UIConstants.LIGHT_BG);
        JLabel box = new JLabel("  ");
        box.setOpaque(true);
        box.setBackground(color);
        box.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JLabel txt = new JLabel(label);
        txt.setFont(UIConstants.SMALL_FONT);
        p.add(box);
        p.add(txt);
        return p;
    }

    // ===== SPOTS =====
    private JPanel buildSpotsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIConstants.LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Manage Parking Spots");
        title.setFont(UIConstants.SUBTITLE_FONT);
        title.setForeground(UIConstants.TEXT_DARK);

        String[] cols = {"Spot ID", "Spot Label", "Area", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        try {
            for (ParkingSpot s : Database.getAllSpots()) {
                model.addRow(new Object[]{
                    s.getSpotId(), s.getSpotLabel(), s.getAreaName(),
                    s.isAvailable() ? "Available" : "Reserved"
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }

        JTable table = buildStyledTable(model);

        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String status = (String) model.getValueAt(row, 3);
                if (!sel) {
                    c.setBackground("Available".equals(status)
                        ? new Color(212, 237, 218)
                        : new Color(248, 215, 218));
                }
                return c;
            }
        });

        JLabel statsLabel = new JLabel();
        statsLabel.setFont(UIConstants.SMALL_FONT);
        statsLabel.setForeground(UIConstants.TEXT_DARK);
        updateSpotsStats(statsLabel, model);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(UIConstants.LIGHT_BG);
        filterPanel.add(new JLabel("Filter by Area:"));

        JComboBox<String> areaFilter = new JComboBox<>();
        areaFilter.addItem("All Areas");
        try {
            for (String[] area : Database.getAllAreas())
                areaFilter.addItem(area[0] + " - " + area[1]);
        } catch (SQLException e) { e.printStackTrace(); }
        areaFilter.setFont(UIConstants.SMALL_FONT);

        areaFilter.addActionListener(e -> {
            String sel = (String) areaFilter.getSelectedItem();
            model.setRowCount(0);
            try {
                for (ParkingSpot s : Database.getAllSpots()) {
                    boolean match = "All Areas".equals(sel)
                        || sel.startsWith(String.valueOf(s.getAreaId()) + " - ");
                    if (match) {
                        model.addRow(new Object[]{
                            s.getSpotId(), s.getSpotLabel(), s.getAreaName(),
                            s.isAvailable() ? "Available" : "Reserved"
                        });
                    }
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
            updateSpotsStats(statsLabel, model);
        });

        filterPanel.add(areaFilter);
        filterPanel.add(statsLabel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        btnPanel.setBackground(UIConstants.LIGHT_BG);
        btnPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        JComboBox<String> addAreaCombo = new JComboBox<>();
        try {
            for (String[] area : Database.getAllAreas())
                addAreaCombo.addItem(area[0] + " - " + area[1]);
        } catch (SQLException e) { e.printStackTrace(); }
        addAreaCombo.setFont(UIConstants.SMALL_FONT);
        addAreaCombo.setPreferredSize(new Dimension(200, 32));

        JButton addBtn = UIConstants.createButton("Add Spot", UIConstants.GREEN_SPOT);
        addBtn.addActionListener(e -> {
            String sel = (String) addAreaCombo.getSelectedItem();
            if (sel == null) return;
            int areaId = Integer.parseInt(sel.split(" - ")[0]);
            try {
                Database.addSpot(areaId);
                JOptionPane.showMessageDialog(panel,
                    "Spot added to \"" + sel.split(" - ", 2)[1] + "\" successfully!");
                // ✅ تحديث فوري بعد الإضافة
                showPanel("spots");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
            }
        });

        JButton deleteBtn = UIConstants.createButton("Delete Selected Spot", UIConstants.RED_SPOT);
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(panel,
                    "Please select a spot from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int spotId    = (int)    model.getValueAt(row, 0);
            String label  = (String) model.getValueAt(row, 1);
            String area   = (String) model.getValueAt(row, 2);
            String status = (String) model.getValueAt(row, 3);

            if ("Reserved".equals(status)) {
                JOptionPane.showMessageDialog(panel,
                    "Cannot delete spot " + label + " — it is currently Reserved!",
                    "Cannot Delete", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(panel,
                "Delete spot \"" + label + "\" from " + area + "?\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    Database.deleteSpot(spotId);
                    // ✅ تحديث فوري بعد الحذف
                    showPanel("spots");
                    JOptionPane.showMessageDialog(panel, "Spot " + label + " deleted.");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
                }
            }
        });

        btnPanel.add(new JLabel("Add to area:"));
        btnPanel.add(addAreaCombo);
        btnPanel.add(addBtn);
        btnPanel.add(Box.createHorizontalStrut(20));
        btnPanel.add(deleteBtn);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(UIConstants.LIGHT_BG);
        northPanel.add(title,       BorderLayout.NORTH);
        northPanel.add(filterPanel, BorderLayout.CENTER);

        panel.add(northPanel,             BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnPanel,               BorderLayout.SOUTH);
        return panel;
    }

    private void updateSpotsStats(JLabel label, DefaultTableModel model) {
        int total = model.getRowCount();
        long avail = 0;
        for (int i = 0; i < total; i++) {
            if ("Available".equals(model.getValueAt(i, 3))) avail++;
        }
        label.setText("  Total: " + total + "  |  Available: " + avail + "  |  Reserved: " + (total - avail));
    }

    // ===== USERS =====
    private JPanel buildUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIConstants.LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Manage Users");
        title.setFont(UIConstants.SUBTITLE_FONT);
        title.setForeground(UIConstants.TEXT_DARK);

        String[] cols = {"ID", "Name", "Email", "ID Number", "Type"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        try {
            for (User u : Database.getAllUsers()) {
                model.addRow(new Object[]{
                    u.getUserId(), u.getUserName(), u.getEmail(),
                    u.getIdNumber(), u.getUserType().toUpperCase()
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }

        JTable table = buildStyledTable(model);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnPanel.setBackground(UIConstants.LIGHT_BG);

        JButton deleteBtn = UIConstants.createButton("Delete User", UIConstants.RED_SPOT);
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(panel, "Select a user first."); return; }
            int userId  = (int)    model.getValueAt(row, 0);
            String name = (String) model.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(panel,
                "Delete user \"" + name + "\" and all their data?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    Database.deleteUser(userId);
                    model.removeRow(row);
                    JOptionPane.showMessageDialog(panel, "User deleted.");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
                }
            }
        });

        JButton refreshBtn = UIConstants.createButton("Refresh", UIConstants.PRIMARY_BLUE);
        refreshBtn.addActionListener(e -> showPanel("users"));

        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);

        panel.add(title,                   BorderLayout.NORTH);
        panel.add(new JScrollPane(table),  BorderLayout.CENTER);
        panel.add(btnPanel,                BorderLayout.SOUTH);
        return panel;
    }

    // ===== HELPERS =====
    private JTable buildStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(UIConstants.SMALL_FONT);
        table.setRowHeight(30);
        table.getTableHeader().setFont(UIConstants.SMALL_FONT);
        table.getTableHeader().setBackground(UIConstants.DARK_BLUE);
        table.getTableHeader().setForeground(UIConstants.WHITE);
        table.setSelectionBackground(new Color(174, 214, 241));
        table.setGridColor(UIConstants.BORDER_COLOR);
        table.setShowGrid(true);
        return table;
    }

    // ✅ FIX: showPanel تبني الـ panel من جديد في كل مرة = بيانات محدّثة بدون logout
    private void showPanel(String name) {
        JPanel fresh = switch (name) {
            case "overview"     -> buildOverviewPanel();
            case "reservations" -> buildReservationsPanel();
            case "spots"        -> buildSpotsPanel();
            case "users"        -> buildUsersPanel();
            default             -> new JPanel();
        };
        contentPanel.add(fresh, name);
        contentLayout.show(contentPanel, name);
    }

    private void startAutoRefresh() {
        refreshTimer = new Timer(60000, e -> {
            try {
                Database.expirePendingReservations();
                Database.releaseExpiredReservations();
                showPanel("overview");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        refreshTimer.start();
    }
}