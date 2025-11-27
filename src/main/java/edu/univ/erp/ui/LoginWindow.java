package edu.univ.erp.ui;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

import edu.univ.erp.api.auth.AuthApi;
import edu.univ.erp.api.types.UserApi;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.AuthService;
import edu.univ.erp.service.UserService;
import edu.univ.erp.util.DataSourceProvider;

public class LoginWindow extends JFrame {

    private final AuthApi authApi;
    private final UserApi userApi;

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginWindow(AuthService authService, UserService userService) {
        this.authApi = new AuthApi(authService);
        this.userApi = new UserApi(userService);
        initUI();
    }

    private void initUI() {
        setTitle("University ERP | Login Page");
        setSize(800, 500);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        Color teal = new Color(63, 173, 168);
        Color darkGrey = new Color(60, 60, 60);
        Color lightGrey = new Color(240, 240, 240);

        //background container
        JPanel bg = new JPanel(new GridBagLayout());
        bg.setBackground(Color.white);
        add(bg, BorderLayout.CENTER);

        //login menu centre card
        JPanel card = new JPanel();
        card.setBackground(Color.white);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(lightGrey, 2),
                BorderFactory.createEmptyBorder(30, 35, 30, 35)
        ));
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(380, 400));
        bg.add(card);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 0, 12, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        //page title
        JLabel title = new JLabel("ERP Login", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(darkGrey);

        gbc.gridy = 0;
        card.add(title, gbc);

        //username label
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        userLabel.setForeground(darkGrey);
        gbc.gridy = 1;
        card.add(userLabel, gbc);

        //username
        usernameField = new JTextField();
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(teal, 2),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridy = 2;
        card.add(usernameField, gbc);

        //password label
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        passLabel.setForeground(darkGrey);
        gbc.gridy = 3;
        card.add(passLabel, gbc);

        //password
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setBackground(Color.white);
        passwordPanel.setBorder(BorderFactory.createLineBorder(teal, 2));

        passwordField = new JPasswordField();
        passwordField.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        //eye button for password
        JButton eyeBtn = getShowBtn();
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(eyeBtn, BorderLayout.EAST);

        gbc.gridy = 4;
        card.add(passwordPanel, gbc);

        //login button
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(teal);
        loginButton.setForeground(Color.white);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        loginButton.setFocusPainted(false);

        loginButton.addActionListener(e -> loginUser());

        gbc.gridy = 5;
        gbc.insets = new Insets(18, 0, 0, 0);
        card.add(loginButton, gbc);
        setLocationRelativeTo(null);
    }

    private JButton getShowBtn() {
        JButton eyeBtn = new JButton("Show" );
        eyeBtn.setPreferredSize(new Dimension(45, 30));
        eyeBtn.setFocusPainted(false);
        eyeBtn.setBorder(null);
        eyeBtn.setBackground(Color.white);

        eyeBtn.addActionListener(e -> {
            if (passwordField.getEchoChar() == 0) {
                passwordField.setEchoChar('•');  // hide
                eyeBtn.setText("Show");
            } else {
                passwordField.setEchoChar((char) 0);  // show
                eyeBtn.setText("Hide");
            }
        });
        return eyeBtn;
    }

    //login
    private void loginUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please Enter Both Username and Password.");
            return;
        }

        //authenticate the user, open dashboard if user authenticated & loaded
        var res= authApi.authenticateUser(username,password);
        if (res.getData() == null) {
            JOptionPane.showMessageDialog(this, res.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
        else{
            User baseUser = res.getData();
            var res2 = userApi.loadUserProfile(baseUser);
            if (res2.getData() == null){
                JOptionPane.showMessageDialog(this, res2.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
            else{
                User full = res2.getData();
                JOptionPane.showMessageDialog(this, "Login Successful!");
                // Open corresponding dashboard
                openDashboard(full);
            }
        }

    }

    //open the dashboard as per role
    private void openDashboard(User user) {
        this.dispose(); // close login window

        switch (user.getRole().toLowerCase()) {
            case "admin":
                SwingUtilities.invokeLater(() -> new AdminDashboard((Admin) user).setVisible(true));
                break;
            case "student":
                SwingUtilities.invokeLater(() -> new StudentDashboard((Student) user).setVisible(true));

                break;
            case "instructor":
                SwingUtilities.invokeLater(() -> new InstructorDashboard((Instructor) user).setVisible(true));
                break;
            default:
                JOptionPane.showMessageDialog(null, "Unknown role: " + user.getRole());
                System.exit(1);
        }
    }
}
