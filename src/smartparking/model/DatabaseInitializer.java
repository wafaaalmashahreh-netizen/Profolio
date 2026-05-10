package smartparking.model;

import java.sql.*;

public class DatabaseInitializer {

    public static void initialize() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            System.err.println("Cannot initialize - no DB connection.");
            return;
        }

        try (Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS USERS (" +
                "    user_id   INT AUTO_INCREMENT PRIMARY KEY," +
                "    user_name VARCHAR(100) NOT NULL," +
                "    email     VARCHAR(100) NOT NULL UNIQUE," +
                "    id_number VARCHAR(50)," +
                "    user_type VARCHAR(20)" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS STUDENT (" +
                "    user_id INT PRIMARY KEY," +
                "    faculty VARCHAR(100)," +
                "    FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE CASCADE ON UPDATE CASCADE" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS EMPLOYEE (" +
                "    user_id    INT PRIMARY KEY," +
                "    department VARCHAR(100)," +
                "    FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE CASCADE ON UPDATE CASCADE" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS VISITOR (" +
                "    user_id     INT PRIMARY KEY," +
                "    passport_id VARCHAR(50)," +
                "    FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE CASCADE ON UPDATE CASCADE" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS ADMIN (" +
                "    user_id INT PRIMARY KEY," +
                "    FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE CASCADE ON UPDATE CASCADE" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS VEHICLE (" +
                "    vehicle_id    INT AUTO_INCREMENT PRIMARY KEY," +
                "    license_plate VARCHAR(50)," +
                "    user_id       INT," +
                "    FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE CASCADE ON UPDATE CASCADE" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS PARKING_AREA (" +
                "    area_id     INT AUTO_INCREMENT PRIMARY KEY," +
                "    name        VARCHAR(100)," +
                "    allowed_for VARCHAR(20) DEFAULT 'all'," +
                "    prefix      VARCHAR(10) DEFAULT 'P'" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS PARKING_SPOT (" +
                "    spot_id      INT AUTO_INCREMENT PRIMARY KEY," +
                "    color_status VARCHAR(50) DEFAULT 'green'," +
                "    spot_label   VARCHAR(10) DEFAULT ''," +
                "    area_id      INT," +
                "    FOREIGN KEY (area_id) REFERENCES PARKING_AREA(area_id) ON DELETE CASCADE ON UPDATE CASCADE" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS RESERVATION (" +
                "    reservation_id INT AUTO_INCREMENT PRIMARY KEY," +
                "    res_date       DATE," +
                "    start_time     TIME," +
                "    end_time       TIME," +
                "    status         VARCHAR(20) DEFAULT 'pending'," +
                "    user_id        INT," +
                "    spot_id        INT," +
                "    vehicle_id     INT," +
                "    expires_at     DATETIME," +
                "    FOREIGN KEY (vehicle_id) REFERENCES VEHICLE(vehicle_id)   ON DELETE CASCADE ON UPDATE CASCADE," +
                "    FOREIGN KEY (user_id)    REFERENCES USERS(user_id)        ON DELETE CASCADE ON UPDATE CASCADE," +
                "    FOREIGN KEY (spot_id)    REFERENCES PARKING_SPOT(spot_id) ON DELETE CASCADE ON UPDATE CASCADE" +
                ")"
            );

            // ✅ NOTIFICATION - العمود الصحيح هو created_at (DATETIME) مش date/res_date
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS NOTIFICATION (" +
                "    notification_id INT AUTO_INCREMENT PRIMARY KEY," +
                "    message         TEXT," +
                "    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "    is_read         BOOLEAN DEFAULT FALSE," +
                "    reservation_id  INT," +
                "    user_id         INT," +
                "    FOREIGN KEY (reservation_id) REFERENCES RESERVATION(reservation_id) ON DELETE CASCADE ON UPDATE CASCADE," +
                "    FOREIGN KEY (user_id)        REFERENCES USERS(user_id)              ON DELETE CASCADE ON UPDATE CASCADE" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS VIOLATION (" +
                "    violation_id INT AUTO_INCREMENT PRIMARY KEY," +
                "    type         VARCHAR(100)," +
                "    fine         DOUBLE," +
                "    paid         BOOLEAN DEFAULT FALSE," +
                "    issued_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    user_id      INT," +
                "    FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE CASCADE ON UPDATE CASCADE" +
                ")"
            );

            // ✅ إذا الجدول موجود بعمود اسمه 'date' بدل 'created_at' - نضيف العمود الجديد
            try {
                stmt.executeUpdate("ALTER TABLE NOTIFICATION ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP");
                System.out.println("Added created_at column to NOTIFICATION.");
            } catch (SQLException ignored) {
                // العمود موجود مسبقاً - طبيعي
            }

            // ✅ إذا الجدول موجود بعمود اسمه 'res_date' - نحاول إضافة created_at بديلاً
            try {
                stmt.executeUpdate("ALTER TABLE NOTIFICATION CHANGE COLUMN `date` created_at DATETIME DEFAULT CURRENT_TIMESTAMP");
                System.out.println("Renamed 'date' column to 'created_at' in NOTIFICATION.");
            } catch (SQLException ignored) {
                // مش موجود أو تم تغييره مسبقاً
            }

            // ✅ إضافة expires_at لجدول RESERVATION إذا مش موجود
            try {
                stmt.executeUpdate("ALTER TABLE RESERVATION ADD COLUMN expires_at DATETIME");
                System.out.println("Added expires_at column to RESERVATION.");
            } catch (SQLException ignored) {
                // موجود مسبقاً
            }

            // Seed parking areas & spots
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM PARKING_AREA");
            rs.next();
            if (rs.getInt(1) == 0) {
                Object[][] areas = {
                    {"External Parking",              "all",              "EX", 40},
                    {"College of Engineering",        "employee_visitor", "EN", 30},
                    {"College of Health Professions", "employee_visitor", "HC", 25},
                    {"College of Science & Tech",     "employee_visitor", "ST", 28},
                    {"College of Dawah & Islamic",    "employee_visitor", "DA", 20},
                    {"College of Arts & Humanities",  "employee_visitor", "AR", 22},
                    {"College of IT",                 "employee_visitor", "IT", 26},
                    {"College of Economics",          "employee_visitor", "EC", 22},
                    {"College of Law",                "employee_visitor", "LW", 20},
                };

                for (Object[] area : areas) {
                    PreparedStatement ps2 = conn.prepareStatement(
                        "INSERT INTO PARKING_AREA (name, allowed_for, prefix) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                    ps2.setString(1, (String) area[0]);
                    ps2.setString(2, (String) area[1]);
                    ps2.setString(3, (String) area[2]);
                    ps2.executeUpdate();

                    ResultSet keys = ps2.getGeneratedKeys();
                    keys.next();
                    int areaId = keys.getInt(1);
                    String prefix = (String) area[2];
                    int count = (int) area[3];

                    for (int i = 1; i <= count; i++) {
                        PreparedStatement sp = conn.prepareStatement(
                            "INSERT INTO PARKING_SPOT (area_id, color_status, spot_label) VALUES (?, 'green', ?)");
                        sp.setInt(1, areaId);
                        sp.setString(2, prefix + i);
                        sp.executeUpdate();
                    }
                }
                System.out.println("Parking areas and spots seeded.");
            }

            // Seed default admin
            ResultSet rsAdmin = stmt.executeQuery("SELECT COUNT(*) FROM ADMIN");
            rsAdmin.next();
            if (rsAdmin.getInt(1) == 0) {
                stmt.executeUpdate(
                    "INSERT INTO USERS (user_name, email, id_number, user_type) " +
                    "VALUES ('Admin', 'wafaaali712004@gmail.com', '22310713', 'admin')"
                );
                ResultSet rsId = stmt.executeQuery(
                    "SELECT user_id FROM USERS WHERE email='wafaaali712004@gmail.com'");
                if (rsId.next()) {
                    stmt.executeUpdate(
                        "INSERT INTO ADMIN (user_id) VALUES (" + rsId.getInt("user_id") + ")");
                }
                System.out.println("Default admin seeded.");
            }

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}