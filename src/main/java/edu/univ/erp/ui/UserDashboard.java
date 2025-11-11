package edu.univ.erp.ui;

import javax.swing.*;
import java.awt.*;

public class UserDashboard extends JFrame {

    public UserDashboard(String username, String role) {
        setTitle("ERP Dashboard - " + username + " (" + role + ")");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel welcome = new JLabel("Welcome, " + username + "!", SwingConstants.CENTER);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel roleLabel = new JLabel("Role: " + role, SwingConstants.CENTER);
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose(); // close dashboard
            new LoginWindow().setVisible(true); // reopen login
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(welcome, BorderLayout.NORTH);
        panel.add(roleLabel, BorderLayout.CENTER);
        panel.add(logoutBtn, BorderLayout.SOUTH);

        add(panel);
    }
}
