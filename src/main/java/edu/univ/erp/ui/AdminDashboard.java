package edu.univ.erp.ui;

import edu.univ.erp.api.auth.AuthApi;
import edu.univ.erp.domain.Admin;
import edu.univ.erp.api.admin.AdminApi;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboard extends JFrame {

    private final Admin adminUser;
    private final AdminApi api;
    private final AuthApi authApi;

    public AdminDashboard(Admin adminUser) {
        this.adminUser = adminUser;
        this.api = new AdminApi(UiContext.get().admin());
        this.authApi = new AuthApi(UiContext.get().auth());

        initUI();
    }

    private void initUI() {
        setTitle("University ERP | Admin Dashboard");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        Color teal = new Color(63, 173, 168);
        Color darkGrey = new Color(50, 50, 50);
        Color lightGrey = new Color(240, 240, 240);

        //top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.white);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Right icon buttons
        JPanel leftIcons = new JPanel (new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftIcons.setOpaque(false);
        JPanel rightIcons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightIcons.setOpaque(false);

        JButton btnNotif = new JButton("Notifications");
        JButton btnSettings = new JButton("Settings");
        JButton btnLogout = new JButton("Log Out");

        JButton[] iconButtons = {btnNotif, btnSettings, btnLogout};
        for (JButton b : iconButtons) {
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            b.setBackground(lightGrey);
            b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        // Logout action
        btnLogout.addActionListener(e -> logout());

        rightIcons.add(btnNotif);
        rightIcons.add(btnSettings);
        rightIcons.add(btnLogout);

        topBar.add(rightIcons, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);


        //left menu
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(lightGrey);
        sidebar.setLayout(new GridBagLayout());
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        // Buttons
        JButton btnAddUser = new JButton("Add User");
        JButton btnAddCourse = new JButton("Add Course");
        JButton btnEditCourse = new JButton("Edit Course");
        JButton btnAddSection = new JButton("Add Section");
        JButton btnEditSection = new JButton("Edit Section");
        JButton btnAssignInstructor = new JButton("Assign Instructor");
        JButton btnMaintenance = new JButton("Toggle Maintenance");

        JButton[] menuButtons = {
                btnAddUser, btnAddCourse, btnAddSection, btnAssignInstructor, btnMaintenance
        };

        for (JButton b : menuButtons) {
            b.setBackground(teal);
            b.setForeground(Color.white);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(teal, 2),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        btnAddUser.addActionListener(e -> openAddUserDialog());

        btnAddCourse.addActionListener(e -> openAddCourseDialog());
        btnAddSection.addActionListener(e -> openAddSectionDialog());
        btnAssignInstructor.addActionListener(e -> openAssignInstructorDialog());
        btnMaintenance.addActionListener(e -> toggleMaintenance());
        btnEditCourse.addActionListener(e -> openEditCourseDialog());

        // Add to sidebar
        gbc.gridy = 0; sidebar.add(btnAddUser, gbc);
        gbc.gridy = 1; sidebar.add(btnAddCourse, gbc);
        gbc.gridy = 2; sidebar.add(btnEditCourse, gbc);
        gbc.gridy = 3; sidebar.add(btnAddSection, gbc);
        gbc.gridy = 5; sidebar.add(btnAssignInstructor, gbc);
        gbc.gridy = 6; sidebar.add(btnMaintenance, gbc);

        add(sidebar, BorderLayout.WEST);

        //centre
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.white);

        JLabel welcome = new JLabel("Welcome, " + adminUser.getUsername());
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 32));
        welcome.setForeground(darkGrey);

        centerPanel.add(welcome);

        add(centerPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    //add user panel
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

        role.addActionListener(e -> {
            if (role.getSelectedIndex() == 0) {
                roll.setEnabled(true);
                program.setEnabled(true);
                year.setEnabled(true);
                dept.setEnabled(false);
                desig.setEnabled(false);
            }
            else if (role.getSelectedIndex() == 1) {
                roll.setEnabled(false);
                program.setEnabled(false);
                year.setEnabled(false);
                dept.setEnabled(true);
                desig.setEnabled(true);
            }
            else {
                roll.setEnabled(false);
                program.setEnabled(false);
                year.setEnabled(false);
                dept.setEnabled(false);
                desig.setEnabled(false);
            }
        });

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

    //add course
    private void openAddCourseDialog() {
        JTextField code = new JTextField();
        JTextField title = new JTextField();
        JComboBox<Integer> credits = new JComboBox<>(new Integer[]{1, 2, 4});

        Object[] form = {
                "Course Code:", code,
                "Title:", title,
                "Credits:", credits
        };

        if (JOptionPane.showConfirmDialog(this, form,
                "Add Course", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            var r = api.addCourse(
                    code.getText(),
                    title.getText(),
                    (Integer) credits.getSelectedItem()
            );

            JOptionPane.showMessageDialog(this, r.getMessage());
        }
    }

    //edit course
    private void openEditCourseDialog() {

        //course list
        List<Course> courses;
        var res = api.listCourses();

        if (!res.isSuccess()) {
            JOptionPane.showMessageDialog(this, res.getMessage());
            return;
        }

        courses = res.getData();

        //course codes
        JComboBox<String> courseDropdown = new JComboBox<>();
        Map<String, Course> courseMap = new HashMap<>();

        for (Course c : courses) {
            String label = c.getCode() + " - " + c.getTitle();
            courseDropdown.addItem(label);
            courseMap.put(label, c);
        }

        //fields
        JTextField titleField = new JTextField();
        JComboBox<Integer> creditsField = new JComboBox<>(new Integer[]{1,2,4});

        //when selecting a course, load the data of that course in form
        courseDropdown.addActionListener(e -> {
            String selected = (String) courseDropdown.getSelectedItem();
            Course c = courseMap.get(selected);

            if (c != null) {
                titleField.setText(c.getTitle());
                creditsField.setSelectedItem(c.getCredits());
            }
        });

        //
        if (courseDropdown.getItemCount() > 0) {
            courseDropdown.setSelectedIndex(0);
            courseDropdown.getActionListeners()[0]
                    .actionPerformed(null); // trigger field fill
        }

        Object[] form = {
                "Select Course:", courseDropdown,
                "Title:", titleField,
                "Credits:", creditsField
        };

        if (JOptionPane.showConfirmDialog(this, form,
                "Edit Course", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            // find selected course
            String selected = (String) courseDropdown.getSelectedItem();
            Course original = courseMap.get(selected);

            // 7. Perform update
            var updateRes = api.updateCourse(
                    original.getCode(),           // code unchanged
                    titleField.getText(),
                    (Integer) creditsField.getSelectedItem()
            );

            JOptionPane.showMessageDialog(this, updateRes.getMessage());
        }
    }

    //add section
    private void openAddSectionDialog() {

        //get courses
        List<Course> courses = api.listCourses().getData();
        JComboBox<String> courseDropdown = new JComboBox<>();
        Map<String, String> courseMap = new HashMap<>();

        for (Course c : courses) {
            String label = c.getCode() + " - " + c.getTitle();
            courseDropdown.addItem(label);
            courseMap.put(label, c.getCode());
        }

        //get instructors
        List<Instructor> instructors = api.listInstructors().getData();
        JComboBox<String> instructorDropdown = new JComboBox<>();
        Map<String, String> instructorMap = new HashMap<>();

        for (Instructor i : instructors) {
            String label = i.getUserId() + " - " + i.getUsername();
            instructorDropdown.addItem(label);
            instructorMap.put(label, i.getUserId());
        }

        JTextField dayTime = new JTextField();
        JTextField room = new JTextField();
        JTextField capacity = new JTextField();
        JTextField semester = new JTextField();
        JTextField year = new JTextField();

        Object[] form = {
                "Course:", courseDropdown,
                "Instructor:", instructorDropdown,
                "Day/Time:", dayTime,
                "Room:", room,
                "Capacity:", capacity,
                "Semester:", semester,
                "Year:", year
        };

        if (JOptionPane.showConfirmDialog(this, form,
                "Add Section", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            String courseKey = (String) courseDropdown.getSelectedItem();
            String instructorKey = (String) instructorDropdown.getSelectedItem();

            var r = api.addSection(
                    courseMap.get(courseKey),
                    instructorMap.get(instructorKey),
                    dayTime.getText(),
                    room.getText(),
                    Integer.parseInt(capacity.getText()),
                    semester.getText(),
                    Integer.parseInt(year.getText())
            );

            JOptionPane.showMessageDialog(this, r.getMessage());
        }
    }

    //edit section
    private void editSectionDialog() {

        // 1. Fetch sections
        var res = api.listSections();
        if (!res.isSuccess()) {
            JOptionPane.showMessageDialog(this, res.getMessage());
            return;
        }

        List<Section> sections = res.getData();
        JComboBox<String> sectionDropdown = new JComboBox<>();
        Map<String, Section> sectionMap = new HashMap<>();

        for (Section s : sections) {
            String label = "Section " + s.getId() + " - " + s.getCourseCode();
            sectionDropdown.addItem(label);
            sectionMap.put(label, s);
        }

        // 2. Fetch courses for dropdown
        List<Course> courses = api.listCourses().getData();
        JComboBox<String> courseDropdown = new JComboBox<>();
        Map<String, Integer> courseMap = new HashMap<>();

        for (Course c : courses) {
            String label = c.getCode() + " - " + c.getTitle();
            courseDropdown.addItem(label);
            courseMap.put(label, c.getId());
        }

        // 3. Fetch instructors
        List<Instructor> instructors = api.listInstructors().getData();
        JComboBox<String> instructorDropdown = new JComboBox<>();
        Map<String, String> instructorMap = new HashMap<>();

        for (Instructor i : instructors) {
            String label = i.getUserId() + " - " + i.getName();
            instructorDropdown.addItem(label);
            instructorMap.put(label, i.getUserId());
        }

        // 4. Fields (empty initially)
        JTextField dayTime = new JTextField();
        JTextField room = new JTextField();
        JTextField capacity = new JTextField();
        JTextField semester = new JTextField();
        JTextField year = new JTextField();

        // 5. Load selected section into fields
        sectionDropdown.addActionListener(e -> {
            Section s = sectionMap.get(sectionDropdown.getSelectedItem());

            // course
            for (String key : courseMap.keySet())
                if (courseMap.get(key) == s.getCourseId())
                    courseDropdown.setSelectedItem(key);

            // instructor
            for (String key : instructorMap.keySet())
                if (instructorMap.get(key).equals(s.getInstructorId()))
                    instructorDropdown.setSelectedItem(key);

            dayTime.setText(s.getDayTime());
            room.setText(s.getRoom());
            capacity.setText(String.valueOf(s.getCapacity()));
            semester.setText(s.getSemester());
            year.setText(String.valueOf(s.getYear()));
        });

        // Trigger initial load
        if (sectionDropdown.getItemCount() > 0)
            sectionDropdown.setSelectedIndex(0);

        Object[] form = {
                "Select Section:", sectionDropdown,
                "Course:", courseDropdown,
                "Instructor:", instructorDropdown,
                "Day/Time:", dayTime,
                "Room:", room,
                "Capacity:", capacity,
                "Semester:", semester,
                "Year:", year
        };

        if (JOptionPane.showConfirmDialog(this, form,
                "Edit Section", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            Section original = sectionMap.get(sectionDropdown.getSelectedItem());

            var r = api.updateSection(
                    original.getId(),
                    courseMap.get(courseDropdown.getSelectedItem()),
                    instructorMap.get(instructorDropdown.getSelectedItem()),
                    dayTime.getText(),
                    room.getText(),
                    Integer.parseInt(capacity.getText()),
                    semester.getText(),
                    Integer.parseInt(year.getText())
            );

            JOptionPane.showMessageDialog(this, r.getMessage());
        }
    }


    //assign instructor
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

    //maintenance toggle
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

    //logout function
    private void logout() {
        var res = authApi.logoutUser();
        if (res.isSuccess()) {
            JOptionPane.showMessageDialog(this, res.getMessage());
            this.dispose();
            UiContext ctx = UiContext.get();
            // open login again
            SwingUtilities.invokeLater(() -> {
                new LoginWindow(ctx.auth(),ctx.users()).setVisible(true);
            });

        }
        else{
            JOptionPane.showMessageDialog(this, res.getMessage());
        }

    }

    //notifications panel
    private void notificationsPanel(){
        return;
    }
}
