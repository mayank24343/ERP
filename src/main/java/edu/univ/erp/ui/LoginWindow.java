package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.AuthResult;
import edu.univ.erp.util.DataSourceProvider;


import javax.sql.DataSource;
import javax.swing.*;
        import java.awt.*;
        import java.awt.event.ActionEvent;

public class LoginWindow extends JFrame {
    private final AuthService authService;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;
    private JButton loginBtn;

    public LoginWindow() {
        DataSource ds = DataSourceProvider.getDataSource();
        this.authService = new AuthService(ds);

        setTitle("University ERP - Login");
        setSize(420, 240);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; p.add(new JLabel("Username:"), c);
        c.gridx = 1; usernameField = new JTextField(20); p.add(usernameField, c);

        c.gridx = 0; c.gridy = 1; p.add(new JLabel("Password:"), c);
        c.gridx = 1; passwordField = new JPasswordField(20); p.add(passwordField, c);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        messageLabel = new JLabel(" ");
        messageLabel.setForeground(Color.RED);
        p.add(messageLabel, c);

        c.gridy = 3; c.gridwidth = 1;
        loginBtn = new JButton("Login");
        loginBtn.addActionListener(this::onLogin);
        p.add(loginBtn, c);

        JButton quit = new JButton("Quit");
        quit.addActionListener(e -> System.exit(0));
        c.gridx = 1;
        p.add(quit, c);

        add(p);
    }

    private void onLogin(ActionEvent ev) {
        loginBtn.setEnabled(false);
        messageLabel.setText("Signing in...");
        SwingUtilities.invokeLater(() -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            AuthResult r = authService.authenticate(username, password);
            if (r.ok) {
                messageLabel.setForeground(new Color(0,128,0));
                messageLabel.setText("Welcome! Role: " + r.role);
                SwingUtilities.invokeLater(() -> {
                    UserDashboard dash = new UserDashboard(username, r.role);
                    dash.setVisible(true);
                });
                dispose();
            } else {
                messageLabel.setForeground(Color.RED);
                if (r.locked) {
                    messageLabel.setText("Account locked until " + r.lockedUntil.toString());
                } else {
                    messageLabel.setText(r.message);
                }
            }
            loginBtn.setEnabled(true);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginWindow w = new LoginWindow();
            w.setVisible(true);
        });
    }
}
