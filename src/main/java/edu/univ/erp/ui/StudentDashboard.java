package edu.univ.erp.ui;

import edu.univ.erp.domain.Admin;
import edu.univ.erp.api.admin.AdminApi;
import edu.univ.erp.domain.Student;
import edu.univ.erp.ui.UiContext;

import javax.swing.*;
import java.awt.*;

public class StudentDashboard extends JFrame {

    private final Student adminUser;
    private final AdminApi api;

    public StudentDashboard(Student adminUser) {
        this.adminUser = adminUser;
        this.api = new AdminApi(UiContext.get().admin());

        initUI();
    }

    private void initUI() {
        setTitle("Admin Dashboard – University ERP");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // Header
        JLabel title = new JLabel("Welcome, " + adminUser.getUsername(),
                SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Buttons panel
        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 40, 40, 40));

        JButton btnAddUser = new JButton("➕ Add User");
        btnAddUser.addActionListener(e -> openAddUserDialog());

        JButton btnAddCourse = new JButton("📘 Add Course");
        btnAddCourse.addActionListener(e -> openAddCourseDialog());

        JButton btnAddSection = new JButton("📚 Add Section");
        btnAddSection.addActionListener(e -> openAddSectionDialog());

        JButton btnAssignInstructor = new JButton("👨‍🏫 Assign Instructor");
        btnAssignInstructor.addActionListener(e -> openAssignInstructorDialog());

        JButton btnMaintenance = new JButton("⚙ Toggle Maintenance Mode");
        btnMaintenance.addActionListener(e -> toggleMaintenance());

        panel.add(btnAddUser);
        panel.add(btnAddCourse);
        panel.add(btnAddSection);
        panel.add(btnAssignInstructor);
        panel.add(btnMaintenance);

        add(panel, BorderLayout.CENTER);
    }

    // -------------------------------
    // DIALOG FOR ADD USER
    // -------------------------------
    private void openAddUserDialog() {
        JTextField username = new JTextField();
        JTextField password = new JTextField();
        JComboBox<String> role = new JComboBox<>(new String[]{"student", "instructor", "admin"});

        JTextField roll = new JTextField();
        JTextField program = new JTextField();
        JTextField year = new JTextField();
        JTextField dept = new JTextField();
        JTextField desig = new JTextField();

        Object[] form = {
                "Username:", username,
                "Password:", password,
                "Role:", role,
                "Student Roll No (if student):", roll,
                "Program (if student):", program,
                "Year (if student):", year,
                "Department (if instructor):", dept,
                "Designation (if instructor):", desig,
        };

        int result = JOptionPane.showConfirmDialog(this, form,
                "Add New User", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            var res = api.addUser(
                    username.getText(),
                    password.getText(),
                    role.getSelectedItem().toString(),
                    roll.getText(),
                    program.getText(),
                    year.getText().isBlank() ? null : Integer.parseInt(year.getText()),
                    dept.getText(),
                    desig.getText()
            );

            JOptionPane.showMessageDialog(this, res.getMessage());
        }
    }

    // -------------------------------
    // ADD COURSE
    // -------------------------------
    private void openAddCourseDialog() {
        JTextField code = new JTextField();
        JTextField title = new JTextField();
        JTextField credits = new JTextField();

        Object[] form = {
                "Course Code:", code,
                "Title:", title,
                "Credits:", credits
        };

        if (JOptionPane.showConfirmDialog(this, form,
                "Add Course", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            var r = api.addCourse(code.getText(), title.getText(),
                    Integer.parseInt(credits.getText()));

            JOptionPane.showMessageDialog(this, r.getMessage());
        }
    }

    // -------------------------------
    // ADD SECTION
    // -------------------------------
    private void openAddSectionDialog() {
        JTextField courseId = new JTextField();
        JTextField instructorId = new JTextField();
        JTextField dayTime = new JTextField();
        JTextField room = new JTextField();
        JTextField capacity = new JTextField();
        JTextField semester = new JTextField();
        JTextField year = new JTextField();

        Object[] form = {
                "Course ID:", courseId,
                "Instructor User ID:", instructorId,
                "Day/Time:", dayTime,
                "Room:", room,
                "Capacity:", capacity,
                "Semester:", semester,
                "Year:", year
        };

        if (JOptionPane.showConfirmDialog(this, form,
                "Add Section", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            var r = api.addSection(
                    Integer.parseInt(courseId.getText()),
                    instructorId.getText(),
                    dayTime.getText(),
                    room.getText(),
                    Integer.parseInt(capacity.getText()),
                    semester.getText(),
                    Integer.parseInt(year.getText())
            );

            JOptionPane.showMessageDialog(this, r.getMessage());
        }
    }

    // -------------------------------
    // ASSIGN INSTRUCTOR
    // -------------------------------
    private void openAssignInstructorDialog() {
        JTextField sectionId = new JTextField();
        JTextField instructorId = new JTextField();

        Object[] form = {
                "Section ID:", sectionId,
                "Instructor User ID:", instructorId
        };

        if (JOptionPane.showConfirmDialog(this, form,
                "Assign Instructor", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            var r = api.assignInstructor(
                    Integer.parseInt(sectionId.getText()),
                    instructorId.getText()
            );

            JOptionPane.showMessageDialog(this, r.getMessage());
        }
    }

    // -------------------------------
    // MAINTENANCE TOGGLE
    // -------------------------------
    private void toggleMaintenance() {
        Object[] options = {"Turn ON", "Turn OFF", "Cancel"};

        int sel = JOptionPane.showOptionDialog(this,
                "Toggle Maintenance Mode:",
                "Maintenance Mode",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        boolean on = (sel == 0); // ON if first button clicked

        var r = api.setMaintenance(on);
        JOptionPane.showMessageDialog(this, r.getMessage());
    }
}
