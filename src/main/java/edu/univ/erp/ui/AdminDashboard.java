package edu.univ.erp.ui;

import edu.univ.erp.api.auth.AuthApi;
import edu.univ.erp.api.maintenance.MaintenanceApi;
import edu.univ.erp.data.CourseDao;
import edu.univ.erp.data.MaintenanceDao;
import edu.univ.erp.domain.Admin;
import edu.univ.erp.api.admin.AdminApi;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AdminDashboard extends JFrame {
    private final Admin adminUser;
    private final AdminApi api;
    private final AuthApi authApi;
    private final MaintenanceApi maintenanceApi;

    //layout
    private JPanel navPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JPanel maintenanceBanner;

    //cards
    private JPanel homeCard;
    private JPanel usersCard;
    private JPanel coursesCard;
    private JPanel sectionsCard;
    private JPanel assignCard;
    private JPanel maintenanceCard;

    //IIIT Delhi colour
    private final Color teal = new Color(63, 173, 168);

    //constructor
    public AdminDashboard(Admin adminUser) {
        super("Admin Dashboard - " + adminUser.getFullname());
        this.adminUser = adminUser;
        this.api = new AdminApi(UiContext.get().admin(), UiContext.get().users());
        this.authApi = new AuthApi(UiContext.get().auth());
        this.maintenanceApi = new MaintenanceApi();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        initUI();
    }

    //initialise UI
    private void initUI() {
        setLayout(new BorderLayout());

        //find if maintenance is on
        boolean maintenanceOn = maintenanceApi.isMaintenanceOn().getData();

        //left side navbar
        navPanel = new JPanel(new GridLayout(10,1,0,5));
        navPanel.setPreferredSize(new Dimension(220,800));
        navPanel.setBackground(teal);
        navPanel.setBorder(BorderFactory.createEmptyBorder(15,10,15,10));

        JButton bUsers = createNavButton("Manage Users");
        JButton bCourses = createNavButton("Manage Courses");
        JButton bSections = createNavButton("Manage Sections");
        JButton bAssign = createNavButton("Assign Instructor");
        JButton bMaintenance = createNavButton("Maintenance Mode");

        bUsers.addActionListener(e -> showCard("users"));
        bCourses.addActionListener(e -> showCard("courses"));
        bSections.addActionListener(e -> showCard("sections"));
        bAssign.addActionListener(e -> showCard("assign"));
        bMaintenance.addActionListener(e -> showCard("maintenance"));

        navPanel.add(bUsers);
        navPanel.add(bCourses);
        navPanel.add(bSections);
        navPanel.add(bAssign);
        navPanel.add(bMaintenance);

        //cards layout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        initHomeCard();
        initUsersCard();
        initCoursesCard();
        initSectionsCard();
        initAssignCard();
        initMaintenanceCard();

        contentPanel.add(homeCard, "home");
        contentPanel.add(usersCard, "users");
        contentPanel.add(coursesCard, "courses");
        contentPanel.add(sectionsCard, "sections");
        contentPanel.add(assignCard, "assign");
        contentPanel.add(maintenanceCard, "maintenance");

        add(createTopBar(adminUser.getFullname()), BorderLayout.NORTH);//topbar
        maintenanceBanner = buildMaintenanceBanner(maintenanceApi.isMaintenanceOn().getData());
        add(maintenanceBanner, BorderLayout.AFTER_LAST_LINE);// maintenance flag
        add(navPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    //maintenance flag
    private JPanel buildMaintenanceBanner(boolean maintenanceOn) {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setPreferredSize(new Dimension(1200, 35));
        banner.setBackground(new Color(230, 160, 0)); // orange

        JLabel label = new JLabel("  Maintenance Mode ON — View Only");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));

        banner.add(label, BorderLayout.CENTER);

        banner.setVisible(maintenanceOn);
        return banner;
    }

    //returns a button for the left side nav with text
    private JButton createNavButton(String text) {
        JButton b = new JButton(text);
        b.putClientProperty("JButton.buttonType", "borderless");
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(new Font("SansSerif", Font.PLAIN, 15));
        b.setForeground(Color.WHITE);
        return b;
    }

    //topbar with full name, change password, and logout functionality
    private JPanel createTopBar(String fullName) {
        JPanel topBar = new JPanel(new BorderLayout());

        topBar.setBackground(teal);
        topBar.setPreferredSize(new Dimension(1200, 80));

        //Welcome User on the left
        JLabel welcome = new JLabel("   Welcome " + fullName);
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 20));

        //change password & logout button on right
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        right.setOpaque(false);

        JButton changePass = new JButton("Change Password");
        JButton logout = new JButton("Logout");

        styleTopbarButton(changePass);
        styleTopbarButton(logout);

        //button click actions
        changePass.addActionListener(e -> openChangePasswordDialog());
        logout.addActionListener(e -> logout());

        right.add(changePass);
        right.add(logout);

        topBar.add(welcome, BorderLayout.WEST);
        topBar.add(right, BorderLayout.EAST);

        return topBar;
    }

    //design for topbar buttons (logout & change password)
    private void styleTopbarButton(JButton b) {
        b.setForeground(Color.WHITE);
        b.setBackground(teal);
        b.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        b.setFocusPainted(false);
        b.putClientProperty("JButton.buttonType", "borderless");
    }

    //password change
    private void openChangePasswordDialog() {
        JPasswordField oldPass = new JPasswordField();
        JPasswordField newPass = new JPasswordField();

        Object[] form = {
                "Old Password:", oldPass,
                "New Password:", newPass
        };

        //show panel & run only if user selects ok
        if (JOptionPane.showConfirmDialog(this, form, "Change Password", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            //change password using auth api
            var res = authApi.changePassword(
                    adminUser.getUsername(),
                    new String(oldPass.getPassword()),
                    new String(newPass.getPassword())
            );

            JOptionPane.showMessageDialog(this, res.getMessage());
        }
    }

    //logout
    private void logout() {
        var res = authApi.logoutUser();
        if (res.isSuccess()){
            JOptionPane.showMessageDialog(this, res.getMessage());
            SwingUtilities.invokeLater( () -> new LoginWindow(UiContext.get().auth(), UiContext.get().users()).setVisible(true));
            dispose();
        }
        else{
            JOptionPane.showMessageDialog(this, res.getMessage());
        }
    }

    //switch card on card layout
    private void showCard(String name) {
        cardLayout.show(contentPanel, name);
    }

    //home
    private void initHomeCard() {
        homeCard = new JPanel(new BorderLayout());
        JLabel msg = new JLabel("Admin Control Center", SwingConstants.CENTER);
        msg.setFont(new Font("SansSerif", Font.BOLD, 28));
        homeCard.add(msg, BorderLayout.CENTER);
    }

    //add and edit users
    private void initUsersCard() {
        usersCard = new JPanel(new BorderLayout());
        usersCard.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JButton add = new JButton("Add User");
        add.addActionListener(e -> openAddUserDialog());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(add);

        usersCard.add(top, BorderLayout.NORTH);
        usersCard.add(new JLabel("User management tools via dialogs."), BorderLayout.CENTER);
    }

    //add and edit courses
    private void initCoursesCard() {
        coursesCard = new JPanel(new BorderLayout());
        coursesCard.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JButton add = new JButton("Add Course");
        JButton edit = new JButton("Edit Course");

        add.addActionListener(e -> openAddCourseDialog());
        edit.addActionListener(e -> openEditCourseDialog());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(add);
        top.add(edit);

        coursesCard.add(top, BorderLayout.NORTH);
        coursesCard.add(new JLabel("Manage courses using the controls above."), BorderLayout.CENTER);
    }

    //add and edit sections
    private void initSectionsCard() {
        sectionsCard = new JPanel(new BorderLayout());
        sectionsCard.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JButton add = new JButton("Add Section");
        JButton edit = new JButton("Edit Section");

        add.addActionListener(e -> openAddSectionDialog());
        edit.addActionListener(e -> openEditSectionDialog());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(add);
        top.add(edit);

        sectionsCard.add(top, BorderLayout.NORTH);
        sectionsCard.add(new JLabel("Manage sections using the controls above."), BorderLayout.CENTER);
    }

    //assign instructor to section
    private void initAssignCard() {
        assignCard = new JPanel(new BorderLayout());
        assignCard.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JButton assign = new JButton("Assign Instructor");
        assign.addActionListener(e -> openAssignInstructorDialog());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(assign);

        assignCard.add(top, BorderLayout.NORTH);
        assignCard.add(new JLabel("Assign instructors to sections."), BorderLayout.CENTER);
    }

    //toggle maintenance
    private void initMaintenanceCard() {
        maintenanceCard = new JPanel(new BorderLayout());
        maintenanceCard.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JButton toggle = new JButton("Toggle Maintenance Mode");
        toggle.addActionListener(e -> toggleMaintenance());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(toggle);

        maintenanceCard.add(top, BorderLayout.NORTH);
        maintenanceCard.add(new JLabel("System maintenance controls."), BorderLayout.CENTER);
    }

    //add user panel
    private void openAddUserDialog() {
        JTextField fullName = new JTextField();
        JTextField username = new JTextField();
        JTextField password = new JTextField();
        JComboBox<String> role = new JComboBox<>(new String[]{"student", "instructor", "admin"});

        JTextField roll = new JTextField();
        JTextField program = new JTextField();
        JTextField year = new JTextField();
        JTextField dept = new JTextField();
        JTextField desig = new JTextField();

        Object[] form = {
                "Full Name:", fullName,
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
                    fullName.getText(),
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
        Map<String, Integer> courseMap = new HashMap<>();

        for (Course c : courses) {
            String label = c.getCode() + " - " + c.getTitle();
            courseDropdown.addItem(label);
            courseMap.put(label, c.getCourseId());
        }

        //get instructors
        List<Instructor> instructors = api.listInstructors().getData();
        JComboBox<String> instructorDropdown = new JComboBox<>();
        Map<String, String> instructorMap = new HashMap<>();

        for (Instructor i : instructors) {
            String label = i.getUsername();
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
    private void openEditSectionDialog() {

        //fetch sections
        var res = api.listSections();
        if (!res.isSuccess()) {
            JOptionPane.showMessageDialog(this, res.getMessage());
            return;
        }

        List<Section> sections = res.getData();
        JComboBox<String> sectionDropdown = new JComboBox<>();
        Map<String, Section> sectionMap = new HashMap<>();

        for (Section s : sections) {
            String label = "Section " + s.getSectionId() + " - " + s.getCourse().getCourseId();
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
            courseMap.put(label, c.getCourseId());
        }

        // 3. Fetch instructors
        List<Instructor> instructors = api.listInstructors().getData();
        JComboBox<String> instructorDropdown = new JComboBox<>();
        Map<String, String> instructorMap = new HashMap<>();

        for (Instructor i : instructors) {
            String label = i.getUsername();
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
                if (courseMap.get(key) == s.getCourse().getCourseId())
                    courseDropdown.setSelectedItem(key);

            // instructor
            for (String key : instructorMap.keySet())
                if (instructorMap.get(key).equals(s.getInstructor()))
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
                    original.getSectionId(),
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
        var res = api.listSections();
        if (!res.isSuccess()) {
            JOptionPane.showMessageDialog(this, res.getMessage());
            return;
        }

        List<Section> sections = res.getData();
        JComboBox<String> sectionDropdown = new JComboBox<>();
        Map<String, Section> sectionMap = new HashMap<>();

        for (Section s : sections) {
            String label = "Section " + s.getSectionId() + " - " + s.getCourse().getCourseId();
            sectionDropdown.addItem(label);
            sectionMap.put(label, s);
        }

        // 3. Fetch instructors
        List<Instructor> instructors = api.listInstructors().getData();
        JComboBox<String> instructorDropdown = new JComboBox<>();
        Map<String, String> instructorMap = new HashMap<>();

        for (Instructor i : instructors) {
            String label = i.getUsername();
            instructorDropdown.addItem(label);
            instructorMap.put(label, i.getUserId());
        }

        Object[] form = {
                "Section ID:", sectionDropdown,
                "Instructor User ID:", instructorDropdown,
        };

        if (JOptionPane.showConfirmDialog(this, form,
                "Assign Instructor", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            var r = api.assignInstructor(
                    sectionMap.get(sectionDropdown.getSelectedItem()).getSectionId(),
                    instructorMap.get(instructorDropdown.getSelectedItem())
            );

            JOptionPane.showMessageDialog(this, r.getMessage());
        }
    }

    //maintenance toggle
    private void toggleMaintenance() {
        Object[] options = {"Turn ON", "Turn OFF"};

        int sel = JOptionPane.showOptionDialog(this,
                "Toggle Maintenance Mode:",
                "Maintenance Mode",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (sel == 2) return;

        boolean on = (sel == 0); //true if first button clicked

        var r = api.setMaintenance(on);

        JOptionPane.showMessageDialog(this, r.getMessage());
        maintenanceBanner.setVisible(on);

        maintenanceBanner.revalidate();
        maintenanceBanner.repaint();
    }
}

