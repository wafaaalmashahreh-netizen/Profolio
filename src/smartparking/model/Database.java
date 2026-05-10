package smartparking.model;

import java.sql.*;
import java.util.*;

public class Database {

    private static Connection conn() {
        return DBConnection.getConnection();
    }

    public static boolean register(User user) throws SQLException {
        String sql = "INSERT INTO USERS (user_name, email, id_number, user_type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUserName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getIdNumber());
            ps.setString(4, user.getUserType());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                user.setUserId(rs.getInt(1));
                insertTypeData(user);
            }
            return true;
        }
    }

    private static void insertTypeData(User user) throws SQLException {
        switch (user.getUserType()) {
            case "student" -> {
                try (PreparedStatement ps = conn().prepareStatement(
                        "INSERT INTO STUDENT (user_id, faculty) VALUES (?, ?)")) {
                    ps.setInt(1, user.getUserId());
                    ps.setString(2, user.getFaculty());
                    ps.executeUpdate();
                }
            }
            case "employee" -> {
                try (PreparedStatement ps = conn().prepareStatement(
                        "INSERT INTO EMPLOYEE (user_id, department) VALUES (?, ?)")) {
                    ps.setInt(1, user.getUserId());
                    ps.setString(2, user.getDepartment());
                    ps.executeUpdate();
                }
            }
            case "visitor" -> {
                try (PreparedStatement ps = conn().prepareStatement(
                        "INSERT INTO VISITOR (user_id, passport_id) VALUES (?, ?)")) {
                    ps.setInt(1, user.getUserId());
                    ps.setString(2, user.getPassportId());
                    ps.executeUpdate();
                }
            }
            case "admin" -> {
                try (PreparedStatement ps = conn().prepareStatement(
                        "INSERT INTO ADMIN (user_id) VALUES (?)")) {
                    ps.setInt(1, user.getUserId());
                    ps.executeUpdate();
                }
            }
        }
    }

    public static User login(String email, String idNumber) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM USERS WHERE email=? AND id_number=?")) {
            ps.setString(1, email);
            ps.setString(2, idNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = mapUser(rs);
                detectAndLoadType(user);
                return user;
            }
        }
        return null;
    }

    private static void detectAndLoadType(User user) throws SQLException {
        int id = user.getUserId();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT user_id FROM ADMIN WHERE user_id=?")) {
            ps.setInt(1, id);
            if (ps.executeQuery().next()) { user.setUserType("admin"); return; }
        }
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT faculty FROM STUDENT WHERE user_id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { user.setUserType("student"); user.setFaculty(rs.getString("faculty")); return; }
        }
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT department FROM EMPLOYEE WHERE user_id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { user.setUserType("employee"); user.setDepartment(rs.getString("department")); return; }
        }
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT passport_id FROM VISITOR WHERE user_id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { user.setUserType("visitor"); user.setPassportId(rs.getString("passport_id")); }
        }
    }

    public static boolean emailExists(String email) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT COUNT(*) FROM USERS WHERE email=?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery(); rs.next();
            return rs.getInt(1) > 0;
        }
    }

    public static boolean idNumberExists(String id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT COUNT(*) FROM USERS WHERE id_number=?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery(); rs.next();
            return rs.getInt(1) > 0;
        }
    }

    public static List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        try (Statement stmt = conn().createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT u.* FROM USERS u " +
                "WHERE u.user_id NOT IN (SELECT user_id FROM ADMIN) " +
                "ORDER BY u.user_id DESC")) {
            while (rs.next()) {
                User user = mapUser(rs);
                detectAndLoadType(user);
                list.add(user);
            }
        }
        return list;
    }

    public static boolean deleteUser(int userId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM USERS WHERE user_id=?")) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    private static User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUserName(rs.getString("user_name"));
        u.setEmail(rs.getString("email"));
        u.setIdNumber(rs.getString("id_number"));
        try { u.setUserType(rs.getString("user_type")); } catch (Exception ignored) {}
        return u;
    }

    // =========================================================
    // VEHICLE
    // =========================================================

    public static boolean addVehicle(String plate, int userId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT COUNT(*) FROM VEHICLE WHERE license_plate=?")) {
            ps.setString(1, plate);
            ResultSet rs = ps.executeQuery(); rs.next();
            if (rs.getInt(1) > 0) return false;
        }
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO VEHICLE (license_plate, user_id) VALUES (?, ?)")) {
            ps.setString(1, plate); ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<Vehicle> getVehiclesByUser(int userId) throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM VEHICLE WHERE user_id=?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(new Vehicle(rs.getInt("vehicle_id"),
                                     rs.getString("license_plate"),
                                     rs.getInt("user_id")));
        }
        return list;
    }

    public static boolean deleteVehicle(int vehicleId, int userId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM VEHICLE WHERE vehicle_id=? AND user_id=?")) {
            ps.setInt(1, vehicleId); ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // PARKING SPOT & AREA
    // =========================================================

    public static List<ParkingSpot> getSpotsByUserType(String userType) throws SQLException {
        String filter = "student".equals(userType)
            ? "pa.allowed_for IN ('student','all')"
            : "pa.allowed_for IN ('employee_visitor','all')";
        return querySpots(
            "SELECT ps.*, pa.name AS area_name FROM PARKING_SPOT ps " +
            "JOIN PARKING_AREA pa ON ps.area_id = pa.area_id " +
            "WHERE " + filter + " ORDER BY ps.area_id, ps.spot_id");
    }

    public static List<ParkingSpot> getAllSpots() throws SQLException {
        return querySpots(
            "SELECT ps.*, pa.name AS area_name FROM PARKING_SPOT ps " +
            "JOIN PARKING_AREA pa ON ps.area_id = pa.area_id " +
            "ORDER BY ps.area_id, ps.spot_id");
    }

    private static List<ParkingSpot> querySpots(String sql) throws SQLException {
        List<ParkingSpot> list = new ArrayList<>();
        try (Statement stmt = conn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ParkingSpot sp = new ParkingSpot();
                sp.setSpotId(rs.getInt("spot_id"));
                sp.setAreaId(rs.getInt("area_id"));
                sp.setAreaName(rs.getString("area_name"));
                sp.setColorStatus(rs.getString("color_status"));
                sp.setSpotLabel(rs.getString("spot_label"));
                list.add(sp);
            }
        }
        return list;
    }

    public static void updateSpotStatus(int spotId, String status) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE PARKING_SPOT SET color_status=? WHERE spot_id=?")) {
            ps.setString(1, status); ps.setInt(2, spotId);
            ps.executeUpdate();
        }
    }

    public static void addSpot(int areaId) throws SQLException {
        String prefix = "P";
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT prefix FROM PARKING_AREA WHERE area_id=?")) {
            ps.setInt(1, areaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) prefix = rs.getString("prefix");
        }
        int next = 1;
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT COALESCE(MAX(CAST(SUBSTRING(spot_label, ?) AS UNSIGNED)), 0) + 1 " +
                "FROM PARKING_SPOT WHERE area_id=?")) {
            ps.setInt(1, prefix.length() + 1);
            ps.setInt(2, areaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) next = rs.getInt(1);
        }
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO PARKING_SPOT (area_id, color_status, spot_label) VALUES (?, 'green', ?)")) {
            ps.setInt(1, areaId); ps.setString(2, prefix + next);
            ps.executeUpdate();
        }
    }

    public static void deleteSpot(int spotId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM PARKING_SPOT WHERE spot_id=?")) {
            ps.setInt(1, spotId); ps.executeUpdate();
        }
    }

    public static List<String[]> getAreasByUserType(String userType) throws SQLException {
        String filter = "student".equals(userType)
            ? "allowed_for IN ('student','all')"
            : "allowed_for IN ('employee_visitor','all')";
        return queryAreas(
            "SELECT area_id, name, allowed_for, prefix FROM PARKING_AREA WHERE "
            + filter + " ORDER BY area_id");
    }

    public static List<String[]> getAllAreas() throws SQLException {
        return queryAreas(
            "SELECT area_id, name, allowed_for, prefix FROM PARKING_AREA ORDER BY area_id");
    }

    private static List<String[]> queryAreas(String sql) throws SQLException {
        List<String[]> list = new ArrayList<>();
        try (Statement stmt = conn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                list.add(new String[]{
                    String.valueOf(rs.getInt("area_id")),
                    rs.getString("name"),
                    rs.getString("allowed_for"),
                    rs.getString("prefix")
                });
        }
        return list;
    }

    // =========================================================
    // RESERVATION
    // =========================================================

    public static boolean hasConflict(int spotId, java.sql.Date date,
                                      java.sql.Time start, java.sql.Time end)
            throws SQLException {
        String sql =
            "SELECT COUNT(*) FROM RESERVATION " +
            "WHERE spot_id=? AND res_date=? " +
            "AND status IN ('pending','confirmed') " +
            "AND start_time < ? AND end_time > ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, spotId);
            ps.setDate(2, date);
            ps.setTime(3, end);
            ps.setTime(4, start);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    public static int createReservation(Reservation res) throws SQLException {
        String sql =
            "INSERT INTO RESERVATION " +
            "(res_date, start_time, end_time, status, user_id, spot_id, vehicle_id, expires_at) " +
            "VALUES (?, ?, ?, 'pending', ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, res.getResDate());
            ps.setTime(2, res.getStartTime());
            ps.setTime(3, res.getEndTime());
            ps.setInt(4, res.getUserId());
            ps.setInt(5, res.getSpotId());
            ps.setInt(6, res.getVehicleId());
            java.time.LocalDateTime expiresAt = java.time.LocalDateTime.of(
                res.getResDate().toLocalDate(),
                res.getStartTime().toLocalTime()
            ).plusMinutes(15);
            ps.setTimestamp(7, Timestamp.valueOf(expiresAt));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    // ✅ التعديل الرئيسي — حذف AND status='pending'
    public static boolean confirmReservation(int resId, int userId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE RESERVATION SET status='confirmed' " +
                "WHERE reservation_id=? AND user_id=?")) {
            ps.setInt(1, resId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean cancelReservation(int resId, int userId) throws SQLException {
        int spotId = -1;
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT spot_id FROM RESERVATION WHERE reservation_id=? AND user_id=?")) {
            ps.setInt(1, resId); ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) spotId = rs.getInt("spot_id");
        }
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE RESERVATION SET status='cancelled' " +
                "WHERE reservation_id=? AND user_id=? AND status IN ('pending','confirmed')")) {
            ps.setInt(1, resId); ps.setInt(2, userId);
            boolean updated = ps.executeUpdate() > 0;
            if (updated && spotId != -1)
                updateSpotStatus(spotId, "green");
            return updated;
        }
    }

    public static List<Reservation> getReservationsByUser(int userId) throws SQLException {
        String sql =
            "SELECT r.*, v.license_plate, pa.name AS area_name " +
            "FROM RESERVATION r " +
            "JOIN VEHICLE v ON r.vehicle_id=v.vehicle_id " +
            "JOIN PARKING_SPOT ps ON r.spot_id=ps.spot_id " +
            "JOIN PARKING_AREA pa ON ps.area_id=pa.area_id " +
            "WHERE r.user_id=? ORDER BY r.res_date DESC, r.start_time DESC";
        List<Reservation> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapReservation(rs));
        }
        return list;
    }

    public static List<Reservation> getAllReservations() throws SQLException {
        String sql =
            "SELECT r.*, u.user_name, v.license_plate, pa.name AS area_name " +
            "FROM RESERVATION r " +
            "JOIN USERS u ON r.user_id=u.user_id " +
            "JOIN VEHICLE v ON r.vehicle_id=v.vehicle_id " +
            "JOIN PARKING_SPOT ps ON r.spot_id=ps.spot_id " +
            "JOIN PARKING_AREA pa ON ps.area_id=pa.area_id " +
            "ORDER BY r.res_date DESC, r.start_time DESC";
        List<Reservation> list = new ArrayList<>();
        try (Statement stmt = conn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Reservation r = mapReservation(rs);
                r.setUserName(rs.getString("user_name"));
                list.add(r);
            }
        }
        return list;
    }

    public static void expirePendingReservations() throws SQLException {
        List<int[]> toExpire = new ArrayList<>();
        try (Statement stmt = conn().createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT reservation_id, spot_id FROM RESERVATION " +
                "WHERE status='pending' AND expires_at < NOW()")) {
            while (rs.next())
                toExpire.add(new int[]{rs.getInt("reservation_id"), rs.getInt("spot_id")});
        }
        for (int[] pair : toExpire) {
            try (PreparedStatement ps = conn().prepareStatement(
                    "UPDATE RESERVATION SET status='expired' WHERE reservation_id=?")) {
                ps.setInt(1, pair[0]); ps.executeUpdate();
            }
            updateSpotStatus(pair[1], "green");
        }
    }

    public static void releaseExpiredReservations() throws SQLException {
        List<int[]> toRelease = new ArrayList<>();
        try (Statement stmt = conn().createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT reservation_id, spot_id FROM RESERVATION " +
                "WHERE status='confirmed' " +
                "AND TIMESTAMP(res_date, end_time) < NOW()")) {
            while (rs.next())
                toRelease.add(new int[]{rs.getInt("reservation_id"), rs.getInt("spot_id")});
        }
        for (int[] pair : toRelease) {
            try (PreparedStatement ps = conn().prepareStatement(
                    "UPDATE RESERVATION SET status='completed' WHERE reservation_id=?")) {
                ps.setInt(1, pair[0]); ps.executeUpdate();
            }
            updateSpotStatus(pair[1], "green");
        }
    }

    private static Reservation mapReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationId(rs.getInt("reservation_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setVehicleId(rs.getInt("vehicle_id"));
        r.setSpotId(rs.getInt("spot_id"));
        r.setResDate(rs.getDate("res_date"));
        r.setStartTime(rs.getTime("start_time"));
        r.setEndTime(rs.getTime("end_time"));
        r.setStatus(rs.getString("status"));
        try { r.setLicensePlate(rs.getString("license_plate")); } catch (Exception ignored) {}
        try { r.setAreaName(rs.getString("area_name")); } catch (Exception ignored) {}
        return r;
    }


    // =========================================================
    // NOTIFICATION
    // =========================================================

    public static void sendNotification(int userId, int reservationId, String message)
            throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO NOTIFICATION (message, is_read, reservation_id, user_id) " +
                "VALUES (?, FALSE, ?, ?)")) {
            ps.setString(1, message);
            ps.setInt(2, reservationId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    public static void sendNotification(int userId, String message) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO NOTIFICATION (message, is_read, reservation_id, user_id) " +
                "VALUES (?, FALSE, NULL, ?)")) {
            ps.setString(1, message);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public static List<Notification> getUnreadByUser(int userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM NOTIFICATION WHERE user_id=? AND is_read=FALSE ORDER BY created_at DESC")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Notification n = new Notification();
                n.setNotificationId(rs.getInt("notification_id"));
                n.setUserId(rs.getInt("user_id"));
                n.setMessage(rs.getString("message"));
                n.setRead(rs.getBoolean("is_read"));
                try {
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) n.setCreatedAt(new java.sql.Date(ts.getTime()));
                } catch (Exception ignored) {}
                list.add(n);
            }
        }
        return list;
    }

    public static void markAllRead(int userId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE NOTIFICATION SET is_read=TRUE WHERE user_id=?")) {
            ps.setInt(1, userId); ps.executeUpdate();
        }
    }
}