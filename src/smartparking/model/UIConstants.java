/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package smartparking.model;


import java.awt.*;

public class UIConstants {
    public static final Color PRIMARY_BLUE = new Color(41, 128, 185);
    public static final Color DARK_BLUE = new Color(21, 67, 99);
    public static final Color LIGHT_BG = new Color(236, 240, 241);
    public static final Color WHITE = Color.WHITE;
    public static final Color GREEN_SPOT = new Color(39, 174, 96);
    public static final Color RED_SPOT = new Color(231, 76, 60);
    public static final Color STUDENT_COLOR = new Color(52, 152, 219);
    public static final Color EMPLOYEE_COLOR = new Color(155, 89, 182);
    public static final Color VISITOR_COLOR = new Color(230, 126, 34);
    public static final Color ADMIN_COLOR = new Color(192, 57, 43);
    public static final Color TEXT_DARK = new Color(44, 62, 80);
    public static final Color BORDER_COLOR = new Color(189, 195, 199);

    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);

    public static javax.swing.JButton createButton(String text, Color bg) {
        javax.swing.JButton btn = new javax.swing.JButton(text);
        btn.setBackground(bg);
        btn.setForeground(WHITE);
        btn.setFont(BUTTON_FONT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 38));
        return btn;
    }

    public static javax.swing.JTextField createTextField() {
        javax.swing.JTextField tf = new javax.swing.JTextField();
        tf.setFont(BODY_FONT);
        tf.setPreferredSize(new Dimension(250, 36));
        tf.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(BORDER_COLOR),
            javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return tf;
    }

    public static javax.swing.JPasswordField createPasswordField() {
        javax.swing.JPasswordField pf = new javax.swing.JPasswordField();
        pf.setFont(BODY_FONT);
        pf.setPreferredSize(new Dimension(250, 36));
        pf.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(BORDER_COLOR),
            javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return pf;
    }
}