package smartparking.model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.List;
import java.util.Map;

public class UserDashboard extends JFrame {

    private final User currentUser;
    private final Color typeColor;
    private JPanel spotsGridPanel;
    private JLabel statusLabel;
    private JComboBox<String> areaCombo;
    private List<ParkingSpot> allSpots;
    private Timer refreshTimer;

    public UserDashboard(User user, Color typeColor) {
        this.currentUser = user;
        this.typeColor   = typeColor;
        setTitle("Smart Parking - " + user.getUserName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        initUI();
        loadSpots();
        startAutoRefresh();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIConstants.LIGHT_BG);
        mainPanel.add(buildTopBar(),    BorderLayout.NORTH);
        mainPanel.add(buildSidePanel(), BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(UIConstants.LIGHT_BG);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel sectionTitle = new JLabel("Parking Spots Overview");
        sectionTitle.setFont(UIConstants.SUBTITLE_FONT);
        sectionTitle.setForeground(UIConstants.TEXT_DARK);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        legendPanel.setBackground(UIConstants.LIGHT_BG);
        legendPanel.add(makeLegendDot(UIConstants.GREEN_SPOT, "Available"));
        legendPanel.add(makeLegendDot(UIConstants.RED_SPOT,   "Reserved"));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(UIConstants.LIGHT_BG);
        topRow.add(sectionTitle, BorderLayout.WEST);
        topRow.add(legendPanel,  BorderLayout.EAST);

        spotsGridPanel = new JPanel();
        spotsGridPanel.setBackground(UIConstants.LIGHT_BG);

        JScrollPane scrollPane = new JScrollPane(spotsGridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        statusLabel = new JLabel("Loading...");
        statusLabel.setFont(UIConstants.SMALL_FONT);
        statusLabel.setForeground(Color.GRAY);

        centerPanel.add(topRow,      BorderLayout.NORTH);
        centerPanel.add(scrollPane,  BorderLayout.CENTER);
        centerPanel.add(statusLabel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(typeColor);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel titleLabel = new JLabel("Smart Parking System");
        titleLabel.setFont(UIConstants.SUBTITLE_FONT);
        titleLabel.setForeground(UIConstants.WHITE);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(typeColor);

        JLabel userLabel = new JLabel("  " + currentUser.getUserName()
            + " (" + currentUser.getUserType() + ")" + getFacultyBadge());
        userLabel.setFont(UIConstants.SMALL_FONT);
        userLabel.setForeground(UIConstants.WHITE);

        JButton notifBtn = new JButton("Notifications");
        notifBtn.setFont(UIConstants.SMALL_FONT);
        notifBtn.setBackground(typeColor.darker());
        notifBtn.setForeground(UIConstants.WHITE);
        notifBtn.setFocusPainted(false);
        notifBtn.setBorderPainted(false);
        notifBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notifBtn.addActionListener(e -> showNotifications());

        JButton logoutBtn = UIConstants.createButton("Logout", UIConstants.TEXT_DARK);
        logoutBtn.addActionListener(e -> logout());

        rightPanel.add(userLabel);
        rightPanel.add(notifBtn);
        rightPanel.add(logoutBtn);

        bar.add(titleLabel, BorderLayout.WEST);
        bar.add(rightPanel,  BorderLayout.EAST);
        return bar;
    }

    private JPanel buildSidePanel() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(UIConstants.WHITE);
        side.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        side.setPreferredSize(new Dimension(220, 0));

        JLabel menuTitle = new JLabel("Menu");
        menuTitle.setFont(UIConstants.SUBTITLE_FONT);
        menuTitle.setForeground(UIConstants.TEXT_DARK);
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        side.add(menuTitle);
        side.add(Box.createVerticalStrut(10));

        if ("student".equals(currentUser.getUserType())) {
            String faculty = currentUser.getFaculty();
            if (faculty != null && !faculty.isEmpty()) {
                String code     = faculty.contains(" - ") ? faculty.split(" - ")[0].trim() : faculty.trim();
                String fullName = faculty.contains(" - ") ? faculty.split(" - ", 2)[1].trim() : faculty.trim();

                JPanel badge = new JPanel();
                badge.setLayout(new BoxLayout(badge, BoxLayout.Y_AXIS));
                badge.setBackground(UIConstants.PRIMARY_BLUE);
                badge.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
                badge.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
                badge.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel codeLbl = new JLabel("[" + code + "]");
                codeLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
                codeLbl.setForeground(UIConstants.WHITE);
                codeLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel nameLbl = new JLabel("<html><small>" + fullName + "</small></html>");
                nameLbl.setFont(UIConstants.SMALL_FONT);
                nameLbl.setForeground(new Color(255, 255, 255, 200));
                nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

                badge.add(codeLbl);
                badge.add(Box.createVerticalStrut(4));
                badge.add(nameLbl);
                side.add(badge);
                side.add(Box.createVerticalStrut(12));
            }
        }

        side.add(Box.createVerticalStrut(5));

        JLabel areaLabel = new JLabel("Filter by Area:");
        areaLabel.setFont(UIConstants.SMALL_FONT);
        areaLabel.setForeground(UIConstants.TEXT_DARK);
        areaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        areaCombo = new JComboBox<>();
        areaCombo.addItem("All Areas");
        areaCombo.setFont(UIConstants.SMALL_FONT);
        areaCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        areaCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        loadAreas();
        areaCombo.addActionListener(e -> loadSpots());

        side.add(areaLabel);
        side.add(Box.createVerticalStrut(5));
        side.add(areaCombo);
        side.add(Box.createVerticalStrut(20));

        String[][] sideButtons = {
            {"My Reservations", "reservations"},
            {"My Vehicles",     "vehicles"},
        };

        for (String[] btnInfo : sideButtons) {
            JButton btn = new JButton(btnInfo[0]);
            btn.setFont(UIConstants.BODY_FONT);
            btn.setBackground(UIConstants.LIGHT_BG);
            btn.setForeground(UIConstants.TEXT_DARK);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            String action = btnInfo[1];
            btn.addActionListener(e -> handleSideAction(action));
            side.add(btn);
            side.add(Box.createVerticalStrut(5));
        }

        side.add(Box.createVerticalGlue());
        return side;
    }

    private void loadAreas() {
        try {
            for (String[] area : Database.getAreasByUserType(currentUser.getUserType()))
                areaCombo.addItem(area[0] + " - " + area[1]);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadSpots() {
        try {
            Database.expirePendingReservations();
            Database.releaseExpiredReservations();
            allSpots = Database.getSpotsByUserType(currentUser.getUserType());

            String selected = (String) areaCombo.getSelectedItem();
            List<ParkingSpot> filtered = allSpots;
            if (selected != null && !selected.equals("All Areas")) {
                String areaId = selected.split(" - ")[0];
                filtered = allSpots.stream()
                    .filter(s -> String.valueOf(s.getAreaId()).equals(areaId))
                    .toList();
            }

            renderSpots(filtered);

            long available = filtered.stream().filter(ParkingSpot::isAvailable).count();
            statusLabel.setText("Total: " + filtered.size()
                + " | Available: " + available
                + " | Reserved: " + (filtered.size() - available));

        } catch (SQLException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void renderSpots(List<ParkingSpot> spots) {
        spotsGridPanel.removeAll();
        spotsGridPanel.setLayout(new BoxLayout(spotsGridPanel, BoxLayout.Y_AXIS));
        spotsGridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (spots.isEmpty()) {
            JLabel empty = new JLabel("No parking spots available for your account type.");
            empty.setFont(UIConstants.BODY_FONT);
            empty.setForeground(Color.GRAY);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            spotsGridPanel.add(empty);
            spotsGridPanel.revalidate();
            spotsGridPanel.repaint();
            return;
        }

        java.util.LinkedHashMap<String, List<ParkingSpot>> grouped = new java.util.LinkedHashMap<>();
        for (ParkingSpot s : spots)
            grouped.computeIfAbsent(s.getAreaName(), k -> new java.util.ArrayList<>()).add(s);

        for (Map.Entry<String, List<ParkingSpot>> entry : grouped.entrySet()) {
            String areaName       = entry.getKey();
            List<ParkingSpot> areaSpots = entry.getValue();
            long avail = areaSpots.stream().filter(ParkingSpot::isAvailable).count();

            JPanel areaHeader = new JPanel(new BorderLayout());
            areaHeader.setBackground(UIConstants.DARK_BLUE);
            areaHeader.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            areaHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            areaHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel areaLabel = new JLabel(getAreaIcon(areaName) + "  " + areaName);
            areaLabel.setFont(UIConstants.SUBTITLE_FONT);
            areaLabel.setForeground(UIConstants.WHITE);

            JLabel countLabel = new JLabel(avail + " / " + areaSpots.size() + " available");
            countLabel.setFont(UIConstants.SMALL_FONT);
            countLabel.setForeground(new Color(200, 230, 200));

            areaHeader.add(areaLabel,  BorderLayout.WEST);
            areaHeader.add(countLabel, BorderLayout.EAST);
            spotsGridPanel.add(areaHeader);
            spotsGridPanel.add(Box.createVerticalStrut(6));

            JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
            grid.setBackground(UIConstants.LIGHT_BG);
            grid.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (ParkingSpot spot : areaSpots) grid.add(createSpotButton(spot));
            spotsGridPanel.add(grid);
            spotsGridPanel.add(Box.createVerticalStrut(12));
        }

        spotsGridPanel.revalidate();
        spotsGridPanel.repaint();
    }

    private String getAreaIcon(String areaName) {
        String n = areaName.toUpperCase();
        if (n.contains("EXTERNAL"))    return "P";
        if (n.contains("ENGINEERING")) return "EN";
        if (n.contains("HEALTH"))      return "HC";
        if (n.contains("SCIENCE"))     return "ST";
        if (n.contains("DAWAH"))       return "DA";
        if (n.contains("ARTS"))        return "AR";
        if (n.contains("IT"))          return "IT";
        if (n.contains("ECONOMICS"))   return "EC";
        if (n.contains("LAW"))         return "LW";
        return "P";
    }

    private JButton createSpotButton(ParkingSpot spot) {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(75, 65));
        btn.setLayout(new BorderLayout());
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        boolean available = spot.isAvailable();
        Color bg = available ? UIConstants.GREEN_SPOT : UIConstants.RED_SPOT;
        btn.setBackground(bg);

        JLabel numLabel = new JLabel(spot.getSpotLabel(), SwingConstants.CENTER);
        numLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        numLabel.setForeground(UIConstants.WHITE);

        JLabel statusLbl = new JLabel(available ? "Free" : "Taken", SwingConstants.CENTER);
        statusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        statusLbl.setForeground(new Color(255, 255, 255, 200));

        JPanel inner = new JPanel(new GridLayout(2, 1));
        inner.setBackground(bg);
        inner.add(numLabel);
        inner.add(statusLbl);
        btn.add(inner, BorderLayout.CENTER);

        btn.setToolTipText(spot.getSpotLabel() + " | " + spot.getAreaName()
            + " | " + (available ? "Available - Click to Reserve" : "Reserved"));

        if (available) {
            btn.addActionListener(e -> showReservationDialog(spot));
            btn.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    btn.setBackground(UIConstants.GREEN_SPOT.darker());
                    inner.setBackground(UIConstants.GREEN_SPOT.darker());
                }
                @Override public void mouseExited(MouseEvent e) {
                    btn.setBackground(UIConstants.GREEN_SPOT);
                    inner.setBackground(UIConstants.GREEN_SPOT);
                }
            });
        }
        return btn;
    }

    private void showReservationDialog(ParkingSpot spot) {
        JDialog dialog = new JDialog(this, "Reserve Spot " + spot.getSpotLabel(), true);
        dialog.setSize(460, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        panel.setBackground(UIConstants.WHITE);

        JLabel title = new JLabel("Reserve Spot " + spot.getSpotLabel());
        title.setFont(UIConstants.SUBTITLE_FONT);
        title.setForeground(typeColor);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel areaInfo = new JLabel("Area: " + spot.getAreaName());
        areaInfo.setFont(UIConstants.SMALL_FONT);
        areaInfo.setForeground(Color.GRAY);
        areaInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField dateField = UIConstants.createTextField();
        dateField.setText(java.time.LocalDate.now().toString());

        JPanel startPanel = buildTimePanel();
        JPanel endPanel   = buildTimePanel();

        ((JSpinner) startPanel.getComponent(0)).setValue(8);
        ((JSpinner) startPanel.getComponent(2)).setValue(0);
        ((JComboBox<?>) startPanel.getComponent(4)).setSelectedItem("AM");
        ((JSpinner) endPanel.getComponent(0)).setValue(10);
        ((JSpinner) endPanel.getComponent(2)).setValue(0);
        ((JComboBox<?>) endPanel.getComponent(4)).setSelectedItem("AM");

        JComboBox<String> vehicleCombo = new JComboBox<>();
        vehicleCombo.setFont(UIConstants.SMALL_FONT);
        try {
            for (Vehicle v : Database.getVehiclesByUser(currentUser.getUserId()))
                vehicleCombo.addItem(v.getVehicleId() + " - " + v.getLicensePlate());
        } catch (SQLException ex) { ex.printStackTrace(); }

        JLabel noteLabel = new JLabel("<html><i>You have 15 minutes after start time to confirm.</i></html>");
        noteLabel.setFont(UIConstants.SMALL_FONT);
        noteLabel.setForeground(Color.GRAY);
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton reserveBtn = UIConstants.createButton("Reserve", typeColor);
        reserveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        reserveBtn.addActionListener(e -> {
            try {
                Date date  = Date.valueOf(dateField.getText().trim());
                Time start = timeFromPanel(startPanel);
                Time end   = timeFromPanel(endPanel);

                if (!end.after(start)) {
                    JOptionPane.showMessageDialog(dialog, "End time must be after start time.");
                    return;
                }

                if (Database.hasConflict(spot.getSpotId(), date, start, end)) {
                    JOptionPane.showMessageDialog(dialog,
                        "This spot is already reserved during the selected time.\nPlease choose a different time.",
                        "Time Conflict", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String vehicleSel = (String) vehicleCombo.getSelectedItem();
                if (vehicleSel == null) {
                    JOptionPane.showMessageDialog(dialog, "Please add a vehicle first.");
                    return;
                }
                int vehicleId = Integer.parseInt(vehicleSel.split(" - ")[0]);

                Reservation res = new Reservation();
                res.setUserId(currentUser.getUserId());
                res.setVehicleId(vehicleId);
                res.setSpotId(spot.getSpotId());
                res.setResDate(date);
                res.setStartTime(start);
                res.setEndTime(end);

                int resId = Database.createReservation(res);
                Database.updateSpotStatus(spot.getSpotId(), "red");
                Database.sendNotification(currentUser.getUserId(), resId,
                    "Reservation #" + resId + " created for Spot " + spot.getSpotLabel()
                    + ". Please confirm within 15 minutes of start time.");

                dialog.dispose();
                loadSpots();

                showConfirmationCountdown(resId, spot, start, date);

            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date format. Use: YYYY-MM-DD");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(areaInfo);
        panel.add(Box.createVerticalStrut(15));
        panel.add(makeRow("Date (YYYY-MM-DD)", dateField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(makeRow("Start Time", startPanel));
        panel.add(Box.createVerticalStrut(10));
        panel.add(makeRow("End Time", endPanel));
        panel.add(Box.createVerticalStrut(10));
        panel.add(makeRow("Vehicle", vehicleCombo));
        panel.add(Box.createVerticalStrut(12));
        panel.add(noteLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(reserveBtn);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private JPanel buildTimePanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setBackground(UIConstants.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(12, 1, 12, 1));
        hourSpinner.setFont(UIConstants.BODY_FONT);
        hourSpinner.setPreferredSize(new Dimension(55, 32));
        ((JSpinner.DefaultEditor) hourSpinner.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);

        JLabel colon = new JLabel(":");
        colon.setFont(UIConstants.SUBTITLE_FONT);

        JSpinner minSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 5));
        minSpinner.setFont(UIConstants.BODY_FONT);
        minSpinner.setPreferredSize(new Dimension(55, 32));
        JSpinner.NumberEditor minEditor = new JSpinner.NumberEditor(minSpinner, "00");
        minSpinner.setEditor(minEditor);
        minEditor.getTextField().setHorizontalAlignment(JTextField.CENTER);

        JComboBox<String> ampm = new JComboBox<>(new String[]{"AM", "PM"});
        ampm.setFont(UIConstants.BODY_FONT);
        ampm.setPreferredSize(new Dimension(60, 32));

        p.add(hourSpinner);
        p.add(colon);
        p.add(minSpinner);
        p.add(Box.createHorizontalStrut(5));
        p.add(ampm);
        return p;
    }

    private Time timeFromPanel(JPanel p) {
        int hour = (int) ((JSpinner) p.getComponent(0)).getValue();
        int min  = (int) ((JSpinner) p.getComponent(2)).getValue();
        String ampm = (String) ((JComboBox<?>) p.getComponent(4)).getSelectedItem();

        if ("AM".equals(ampm)) {
            if (hour == 12) hour = 0;
        } else {
            if (hour != 12) hour += 12;
        }
        return Time.valueOf(String.format("%02d:%02d:00", hour, min));
    }

    private void showConfirmationCountdown(int resId, ParkingSpot spot,
                                            Time startTime, Date resDate) {
        java.time.LocalDateTime startDateTime = java.time.LocalDateTime.of(
            resDate.toLocalDate(), startTime.toLocalTime());
        java.time.LocalDateTime expiresAt = startDateTime.plusMinutes(15);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        long secondsLeft;
        if (now.isBefore(startDateTime)) {
            secondsLeft = 15 * 60;
        } else {
            secondsLeft = java.time.Duration.between(now, expiresAt).getSeconds();
            if (secondsLeft <= 0) secondsLeft = 15 * 60;
        }

        final long[] remaining = {secondsLeft};

        JDialog countdownDialog = new JDialog(this, "Confirm Your Reservation", false);
        countdownDialog.setSize(380, 280);
        countdownDialog.setLocationRelativeTo(this);
        countdownDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setBackground(UIConstants.WHITE);

        JLabel resLabel = new JLabel("Reservation #" + resId + " Created!");
        resLabel.setFont(UIConstants.SUBTITLE_FONT);
        resLabel.setForeground(typeColor);
        resLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel spotLabel = new JLabel("Spot: " + spot.getSpotLabel() + "  |  " + spot.getAreaName());
        spotLabel.setFont(UIConstants.SMALL_FONT);
        spotLabel.setForeground(Color.GRAY);
        spotLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel timerTitle = new JLabel("Time remaining to confirm:");
        timerTitle.setFont(UIConstants.BODY_FONT);
        timerTitle.setForeground(UIConstants.TEXT_DARK);
        timerTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel timerLabel = new JLabel(formatTime(remaining[0]));
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        timerLabel.setForeground(UIConstants.GREEN_SPOT);
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton confirmBtn = UIConstants.createButton("✔ Confirm Now", UIConstants.GREEN_SPOT);
        confirmBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton cancelBtn = UIConstants.createButton("✖ Cancel", UIConstants.RED_SPOT);
        cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setBackground(UIConstants.WHITE);
        btnRow.add(confirmBtn);
        btnRow.add(cancelBtn);

        panel.add(resLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(spotLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(timerTitle);
        panel.add(Box.createVerticalStrut(8));
        panel.add(timerLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(btnRow);

        countdownDialog.add(panel);

        Timer countdown = new Timer(1000, null);
        countdown.addActionListener(e -> {
            remaining[0]--;
            timerLabel.setText(formatTime(remaining[0]));

            if (remaining[0] <= 60) {
                timerLabel.setForeground(UIConstants.RED_SPOT);
            } else if (remaining[0] <= 180) {
                timerLabel.setForeground(new Color(230, 126, 34));
            }

            if (remaining[0] <= 0) {
                countdown.stop();
                try {
                    Connection conn = DBConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(
                        "UPDATE RESERVATION SET status='expired' WHERE reservation_id=? AND status='pending'");
                    ps.setInt(1, resId);
                    int updated = ps.executeUpdate();
                    if (updated > 0) {
                        Database.updateSpotStatus(spot.getSpotId(), "green");
                        Database.sendNotification(currentUser.getUserId(), resId,
                            "Reservation #" + resId + " expired - not confirmed within 15 minutes.");
                    }
                } catch (SQLException ex) { ex.printStackTrace(); }

                countdownDialog.dispose();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(UserDashboard.this,
                        "Reservation #" + resId + " was automatically cancelled.\n(15 minutes confirmation window expired)",
                        "Reservation Expired", JOptionPane.WARNING_MESSAGE);
                    loadSpots();
                });
            }
        });
        countdown.start();

        confirmBtn.addActionListener(e -> {
            countdown.stop();
            try {
                boolean confirmed = Database.confirmReservation(resId, currentUser.getUserId());
                if (confirmed) {
                    Database.sendNotification(currentUser.getUserId(), resId,
                        "Reservation #" + resId + " CONFIRMED! Spot " + spot.getSpotLabel() + " is yours.");
                    countdownDialog.dispose();
                    JOptionPane.showMessageDialog(UserDashboard.this,
                        "Reservation #" + resId + " confirmed successfully!",
                        "Confirmed", JOptionPane.INFORMATION_MESSAGE);
                    loadSpots();
                } else {
                    JOptionPane.showMessageDialog(countdownDialog, "Could not confirm reservation.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(countdownDialog, "Error: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> {
            countdown.stop();
            try {
                Database.cancelReservation(resId, currentUser.getUserId());
                Database.sendNotification(currentUser.getUserId(), resId,
                    "Reservation #" + resId + " was cancelled.");
                countdownDialog.dispose();
                loadSpots();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(countdownDialog, "Error: " + ex.getMessage());
            }
        });

        countdownDialog.setVisible(true);
    }

    private String formatTime(long totalSeconds) {
        if (totalSeconds < 0) totalSeconds = 0;
        long mins = totalSeconds / 60;
        long secs = totalSeconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    private JPanel makeRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setBackground(UIConstants.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        JLabel label = new JLabel(labelText);
        label.setFont(UIConstants.SMALL_FONT);
        label.setForeground(UIConstants.TEXT_DARK);
        row.add(label, BorderLayout.NORTH);
        row.add(field,  BorderLayout.CENTER);
        return row;
    }

    private void handleSideAction(String action) {
        switch (action) {
            case "reservations" -> showMyReservations();
            case "vehicles"     -> showMyVehicles();
        }
    }

    private void showMyReservations() {
        try {
            List<Reservation> list = Database.getReservationsByUser(currentUser.getUserId());
            String[] cols = {"ID", "Date", "Start", "End", "Spot", "Area", "Vehicle", "Status"};
            Object[][] data = new Object[list.size()][8];
            for (int i = 0; i < list.size(); i++) {
                Reservation r = list.get(i);
                data[i] = new Object[]{
                    r.getReservationId(), r.getResDate(),
                    formatSqlTimeToAmPm(r.getStartTime()),
                    formatSqlTimeToAmPm(r.getEndTime()),
                    r.getSpotId(), r.getAreaName(),
                    r.getLicensePlate(), r.getStatus().toUpperCase()
                };
            }

            JTable table = new JTable(data, cols);
            table.setFont(UIConstants.SMALL_FONT);
            table.setRowHeight(28);
            table.setDefaultEditor(Object.class, null);

            JDialog dialog = new JDialog(this, "My Reservations", true);
            dialog.setSize(800, 400);
            dialog.setLocationRelativeTo(this);

            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            JButton cancelBtn = UIConstants.createButton("Cancel Selected", UIConstants.RED_SPOT);
            cancelBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(dialog, "Select a reservation first."); return; }
                int resId     = (int) data[row][0];
                String status = (String) data[row][7];
                if (status.equals("CANCELLED") || status.equals("EXPIRED") || status.equals("COMPLETED")) {
                    JOptionPane.showMessageDialog(dialog, "Reservation is already " + status + "."); return;
                }
                try {
                    Database.cancelReservation(resId, currentUser.getUserId());
                    JOptionPane.showMessageDialog(dialog, "Reservation cancelled.");
                    dialog.dispose();
                    loadSpots();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                }
            });

            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.add(cancelBtn, BorderLayout.SOUTH);
            dialog.add(panel);
            dialog.setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private String formatSqlTimeToAmPm(java.sql.Time t) {
        if (t == null) return "";
        java.time.LocalTime lt = t.toLocalTime();
        int hour = lt.getHour();
        int min  = lt.getMinute();
        String ampm = hour < 12 ? "AM" : "PM";
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        return String.format("%d:%02d %s", displayHour, min, ampm);
    }

    private void showMyVehicles() {
        try {
            List<Vehicle> vehicles = Database.getVehiclesByUser(currentUser.getUserId());
            String[] cols = {"Vehicle ID", "License Plate"};
            Object[][] data = new Object[vehicles.size()][2];
            for (int i = 0; i < vehicles.size(); i++)
                data[i] = new Object[]{vehicles.get(i).getVehicleId(), vehicles.get(i).getLicensePlate()};

            JTable table = new JTable(data, cols);
            table.setFont(UIConstants.SMALL_FONT);
            table.setRowHeight(28);
            table.setDefaultEditor(Object.class, null);

            JDialog dialog = new JDialog(this, "My Vehicles", true);
            dialog.setSize(450, 400);
            dialog.setLocationRelativeTo(this);

            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            JTextField plateField = UIConstants.createTextField();
            plateField.setPreferredSize(new Dimension(150, 36));

            JButton addBtn = UIConstants.createButton("Add Vehicle", typeColor);
            addBtn.addActionListener(e -> {
                String plate = plateField.getText().trim().toUpperCase();
                if (plate.isEmpty()) return;
                if (!Validator.isValidLicensePlate(plate)) {
                    JOptionPane.showMessageDialog(dialog, "Invalid license plate format.");
                    return;
                }
                try {
                    boolean added = Database.addVehicle(plate, currentUser.getUserId());
                    if (!added) JOptionPane.showMessageDialog(dialog, "License plate already exists.");
                    else { JOptionPane.showMessageDialog(dialog, "Vehicle added!"); dialog.dispose(); }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                }
            });

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            btnPanel.add(new JLabel("Plate:"));
            btnPanel.add(plateField);
            btnPanel.add(addBtn);

            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.add(btnPanel, BorderLayout.SOUTH);
            dialog.add(panel);
            dialog.setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void showNotifications() {
        try {
            List<Notification> notifs = Database.getUnreadByUser(currentUser.getUserId());
            Database.markAllRead(currentUser.getUserId());

            if (notifs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No new notifications.", "Notifications",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            StringBuilder sb = new StringBuilder("<html><body style='width:350px;font-family:Segoe UI;'>");
            for (Notification n : notifs) {
                sb.append("<p style='margin:8px 0;border-bottom:1px solid #eee;padding-bottom:6px;'>")
                  .append(n.getMessage())
                  .append("<br><small style='color:gray;'>").append(n.getCreatedAt()).append("</small></p>");
            }
            sb.append("</body></html>");

            JLabel label = new JLabel(sb.toString());
            JScrollPane sp = new JScrollPane(label);
            sp.setPreferredSize(new Dimension(400, 300));
            JOptionPane.showMessageDialog(this, sp,
                "Notifications (" + notifs.size() + ")", JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private JPanel makeLegendDot(Color color, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setBackground(UIConstants.LIGHT_BG);
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        dot.setForeground(color);
        JLabel text = new JLabel(label);
        text.setFont(UIConstants.SMALL_FONT);
        text.setForeground(UIConstants.TEXT_DARK);
        p.add(dot);
        p.add(text);
        return p;
    }

    private String getFacultyBadge() {
        if (!"student".equals(currentUser.getUserType())) return "";
        String faculty = currentUser.getFaculty();
        if (faculty == null || faculty.isEmpty()) return "";
        String code = faculty.contains(" - ") ? faculty.split(" - ")[0].trim() : faculty.trim();
        return "  [" + code + "]";
    }

    private void startAutoRefresh() {
        refreshTimer = new Timer(30000, e -> {
            try {
                Database.expirePendingReservations();
                Database.releaseExpiredReservations();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            loadSpots();
        });
        refreshTimer.start();
    }

    private void logout() {
        if (refreshTimer != null) refreshTimer.stop();
        dispose();
        new WelcomeFrame().setVisible(true);
    }
}
