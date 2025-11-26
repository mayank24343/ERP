package edu.univ.erp.ui;

import edu.univ.erp.api.auth.AuthApi;
import edu.univ.erp.api.maintenance.MaintenanceApi;
import edu.univ.erp.api.student.StudentApi;
import edu.univ.erp.api.common.ApiResult;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.util.CSVExporter;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDashboard extends JFrame {

    private final Student studentUser;
    private final StudentApi api;
    private final AuthApi authApi;
    private final MaintenanceApi maintenanceApi;

    private JPanel navPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    private JPanel catalogCard;
    private JPanel mySectionsCard;
    private JPanel registerCard;
    private JPanel timetableCard;
    private JPanel gradesCard;

    private JTable catalogTable;
    private JTable mySectionsTable;
    private JTable finalGradesTable;
    private JTable breakdownTable;

    private CatalogTableModel catalogModel;
    private MySectionsTableModel mySectionsModel;
    private FinalGradesTableModel finalGradesModel;
    private BreakdownTableModel breakdownModel;

    private JComboBox<String> courseDropdown;
    private JTable availableSectionsTable;
    private AvailableSectionsTableModel availableSectionsModel;

    private final Color teal = new Color(63, 173, 168);

    //constructor
    public StudentDashboard(Student studentUser) {
        super("Student Dashboard - " + studentUser.getUsername());
        this.studentUser = studentUser;
        this.api = new StudentApi(UiContext.get().students());
        this.authApi = new AuthApi(UiContext.get().auth());
        this.maintenanceApi = new MaintenanceApi();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);


        initUI();
        loadCatalog();
    }

    //ui
    private void initUI() {
        setLayout(new BorderLayout());
        boolean maintenanceOn = maintenanceApi.isMaintenanceOn().getData();
        add(createTopBar(studentUser.getFullname()), BorderLayout.NORTH);
        add(buildMaintenanceBanner(maintenanceOn), BorderLayout.AFTER_LAST_LINE);

        navPanel = new JPanel(new GridLayout(10, 1, 0, 5));
        navPanel.setPreferredSize(new Dimension(220, 800));
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        navPanel.setBackground(teal);

        JButton b1 = createNavButton("Course Catalog");
        JButton b2 = createNavButton("My Sections");
        JButton b3 = createNavButton("Register");
        JButton b4 = createNavButton("Timetable");
        JButton b5 = createNavButton("Grades");

        b1.addActionListener(e -> showCard("catalog"));
        b2.addActionListener(e -> showCard("mysections"));
        b3.addActionListener(e -> showCard("register"));
        b4.addActionListener(e -> showCard("timetable"));
        b5.addActionListener(e -> showCard("grades"));

        navPanel.add(b1);
        navPanel.add(b2);
        navPanel.add(b3);
        navPanel.add(b4);
        navPanel.add(b5);

        add(navPanel, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        buildCatalogCard();
        buildMySectionsCard();
        buildRegisterCard();
        buildTimetableCard();
        buildGradesCard();

        contentPanel.add(catalogCard, "catalog");
        contentPanel.add(mySectionsCard, "mysections");
        contentPanel.add(registerCard, "register");
        contentPanel.add(timetableCard, "timetable");
        contentPanel.add(gradesCard, "grades");

        add(contentPanel, BorderLayout.CENTER);
    }

    //maintenance flag
    private JPanel buildMaintenanceBanner(boolean maintenanceOn) {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setPreferredSize(new Dimension(1200, 40));

        if (!maintenanceOn) {
            banner.setVisible(false);
            return banner;
        }

        banner.setVisible(true);
        banner.setBackground(new Color(230, 160, 0)); // orange warning

        JLabel label = new JLabel(" ⚠ Maintenance Mode Active — System is in View-Only Mode");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 15));

        banner.add(label, BorderLayout.CENTER);
        return banner;
    }


    //returns a buttonn for the left side nav
    private JButton createNavButton(String text) {
        JButton b = new JButton(text);
        b.putClientProperty("JButton.buttonType", "borderless");
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(new Font("SansSerif", Font.PLAIN, 16));
        return b;
    }

    //Topbar with full name, change password, and logout functionality
    private JPanel createTopBar(String fullName) {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(0, 128, 128)); // teal
        topBar.setPreferredSize(new Dimension(1200, 50));

        // LEFT: Welcome name
        JLabel welcome = new JLabel("   Welcome " + fullName);
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 18));

        // RIGHT: Change Password | Logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        right.setOpaque(false); // transparent so teal shows

        JButton changePass = new JButton("Change Password");
        JButton logout = new JButton("Logout");

        styleTopbarButton(changePass);
        styleTopbarButton(logout);

        // Add actions
        changePass.addActionListener(e -> openChangePasswordDialog());
        logout.addActionListener(e -> logoutToLogin());

        right.add(changePass);
        right.add(logout);

        topBar.add(welcome, BorderLayout.WEST);
        topBar.add(right, BorderLayout.EAST);

        return topBar;
    }

    //design for topbar buttons (logout & change password)
    private void styleTopbarButton(JButton b) {
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(0, 128, 128));
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

        if (JOptionPane.showConfirmDialog(this, form,
                "Change Password", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            //change password using auth api
            var res = authApi.changePassword(
                    studentUser.getUsername(),
                    new String(oldPass.getPassword()),
                    new String(newPass.getPassword())
            );

            JOptionPane.showMessageDialog(this, res.getMessage());
        }
    }

    //logout
    private void logoutToLogin() {
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

    //ui card shown beside the left navbar
    private void showCard(String name) {
        cardLayout.show(contentPanel, name);
    }

    //course catalog card
    private void buildCatalogCard() {
        catalogCard = new JPanel(new BorderLayout());
        catalogCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        catalogModel = new CatalogTableModel();
        catalogTable = new JTable(catalogModel);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadCatalog());

        catalogCard.add(refresh, BorderLayout.NORTH);
        catalogCard.add(new JScrollPane(catalogTable), BorderLayout.CENTER);
    }

    //loads the courses for the table, (into catalog model, which is then added to the table)
    private void loadCatalog() {
        ApiResult<List<Course>> r = api.catalog();
        if (!r.isSuccess()) {
            JOptionPane.showMessageDialog(this, r.getMessage());
            return;
        }
        catalogModel.setData(r.getData());
        courseDropdown.removeAllItems();
        for (Course c : r.getData()) {
            courseDropdown.addItem(c.getCode() + " - " + c.getTitle());
        }

    }

    //enrolled sections card
    private void buildMySectionsCard() {
        mySectionsCard = new JPanel(new BorderLayout());
        mySectionsCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        mySectionsModel = new MySectionsTableModel();
        mySectionsTable = new JTable(mySectionsModel);

        JButton refresh = new JButton("Refresh");
        JButton drop = new JButton("Drop Section");

        refresh.addActionListener(e -> loadMySections());
        drop.addActionListener(e -> dropSelectedSection());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refresh);
        top.add(drop);

        mySectionsCard.add(top, BorderLayout.NORTH);
        mySectionsCard.add(new JScrollPane(mySectionsTable), BorderLayout.CENTER);
    }

    //load sections
    private void loadMySections() {
        ApiResult<List<Section>> r = api.mySections(studentUser.getUserId());
        if (!r.isSuccess()) {
            JOptionPane.showMessageDialog(this, r.getMessage());
            return;
        }
        mySectionsModel.setData(r.getData());
    }

    //drop a section
    private void dropSelectedSection() {
        int row = mySectionsTable.getSelectedRow();
        if (row < 0) return;

        Section s = mySectionsModel.get(row);

        ApiResult<String> r = api.drop(studentUser.getUserId(), s.getSectionId());
        JOptionPane.showMessageDialog(this, r.getMessage());
        loadMySections();
    }

    //registration card
    private void buildRegisterCard() {
        registerCard = new JPanel(new BorderLayout());
        registerCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        courseDropdown = new JComboBox<>();
        JButton loadSections = new JButton("Load Sections");

        loadSections.addActionListener(e -> loadAvailableSections());

        top.add(new JLabel("Select Course:"));
        top.add(courseDropdown);
        top.add(loadSections);

        availableSectionsModel = new AvailableSectionsTableModel();
        availableSectionsTable = new JTable(availableSectionsModel);

        JButton register = new JButton("Register");
        register.addActionListener(e -> registerForSelectedSection());

        registerCard.add(top, BorderLayout.NORTH);
        registerCard.add(new JScrollPane(availableSectionsTable), BorderLayout.CENTER);
        registerCard.add(register, BorderLayout.SOUTH);
    }

    //load the available sections
    private void loadAvailableSections() {
        int idx = courseDropdown.getSelectedIndex();
        if (idx < 0) return;

        Course c = catalogModel.get(idx);

        ApiResult<List<Section>> r = api.getAvailableSections(c.getCourseId());
        if (!r.isSuccess()) {
            JOptionPane.showMessageDialog(this, r.getMessage());
            return;
        }

        availableSectionsModel.setData(r.getData());
    }

    //register for a section
    private void registerForSelectedSection() {
        int row = availableSectionsTable.getSelectedRow();
        if (row < 0) return;

        Section s = availableSectionsModel.get(row);

        ApiResult<String> r = api.register(studentUser.getUserId(), s.getSectionId());
        JOptionPane.showMessageDialog(this, r.getMessage());
    }

    //timetable card
    private void buildTimetableCard() {
        timetableCard = new JPanel(new BorderLayout());
        timetableCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTable table = new JTable();
        timetableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton load = new JButton("Load Timetable");
        load.addActionListener(e -> {
            ApiResult<List<Section>> r = api.timetable(studentUser.getUserId());
            if (!r.isSuccess()) {
                JOptionPane.showMessageDialog(this, r.getMessage());
                return;
            }

            MySectionsTableModel m = new MySectionsTableModel();
            m.setData(r.getData());
            table.setModel(m);
        });

        timetableCard.add(load, BorderLayout.NORTH);
    }

    //grades page card
    private void buildGradesCard() {
        gradesCard = new JPanel(new BorderLayout());
        gradesCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        finalGradesModel = new FinalGradesTableModel();
        finalGradesTable = new JTable(finalGradesModel);

        breakdownModel = new BreakdownTableModel();
        breakdownTable = new JTable(breakdownModel);

        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(finalGradesTable),
                new JScrollPane(breakdownTable)
        );
        split.setDividerLocation(250);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadFinals = new JButton("Load Final Grades");
        JButton loadBreakdown = new JButton("Load Breakdown");
        JButton exportFinals = new JButton("Export Final Grades");
        exportFinals.addActionListener(e -> CSVExporter.exportTable(finalGradesTable,
                "my_final_grades"));


        loadFinals.addActionListener(e -> loadFinalGrades());
        loadBreakdown.addActionListener(e -> loadBreakdown());

        top.add(loadFinals);
        top.add(loadBreakdown);
        top.add(exportFinals);

        gradesCard.add(top, BorderLayout.NORTH);
        gradesCard.add(split, BorderLayout.CENTER);
    }

    //load final grades
    private void loadFinalGrades() {
        ApiResult<List<FinalGrade>> r = api.finalGrades(studentUser.getUserId());
        if (!r.isSuccess()) {
            JOptionPane.showMessageDialog(this, r.getMessage());
            return;
        }
        finalGradesModel.setData(r.getData());
    }

    //load breakdown per course
    private void loadBreakdown() {
        ApiResult<List<StudentService.GradeView>> r = api.gradeBreakdown(studentUser.getUserId());
        if (!r.isSuccess()) {
            JOptionPane.showMessageDialog(this, r.getMessage());
            return;
        }
        breakdownModel.setData(r.getData());
    }

    //model for tables
    private static class CatalogTableModel extends AbstractTableModel {
        private final String[] cols = {"Course ID", "Code", "Title", "Credits"};
        private List<Course> data = new ArrayList<>();

        public void setData(List<Course> d) {
            data = d;
            fireTableDataChanged();
        }

        public Course get(int r) { return data.get(r); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            Course x = data.get(r);
            return switch (c) {
                case 0 -> x.getCourseId();
                case 1 -> x.getCode();
                case 2 -> x.getTitle();
                case 3 -> x.getCredits();
                default -> null;
            };
        }
    }

    private static class MySectionsTableModel extends AbstractTableModel {
        private final String[] cols = {"Section ID", "Course ID", "Day/Time", "Room"};
        private List<Section> data = new ArrayList<>();

        public void setData(List<Section> d) {
            data = d;
            fireTableDataChanged();
        }

        public Section get(int r) { return data.get(r); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            Section s = data.get(r);
            return switch (c) {
                case 0 -> s.getSectionId();
                case 1 -> s.getCourseId();
                case 2 -> s.getDayTime();
                case 3 -> s.getRoom();
                default -> null;
            };
        }
    }

    private static class AvailableSectionsTableModel extends AbstractTableModel {
        private final String[] cols = {"Section ID", "Instructor", "Time", "Room", "Capacity"};
        private List<Section> data = new ArrayList<>();

        public void setData(List<Section> d) {
            data = d;
            fireTableDataChanged();
        }

        public Section get(int r) { return data.get(r); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            Section s = data.get(r);
            return switch (c) {
                case 0 -> s.getSectionId();
                case 1 -> s.getInstructorId();
                case 2 -> s.getDayTime();
                case 3 -> s.getRoom();
                case 4 -> s.getCapacity();
                default -> null;
            };
        }
    }

    private static class FinalGradesTableModel extends AbstractTableModel {
        private final String[] cols = {"Section", "Percentage", "Grade"};
        private List<FinalGrade> data = new ArrayList<>();

        public void setData(List<FinalGrade> d) {
            data = d;
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            FinalGrade x = data.get(r);
            return switch (c) {
                case 0 -> x.getSectionId();
                case 1 -> x.getPercentage();
                case 2 -> x.getLetter();
                default -> null;
            };
        }
    }

    private static class BreakdownTableModel extends AbstractTableModel {
        private final String[] cols = {"Section", "Assessment", "Score", "Final %"};
        private List<StudentService.GradeView> data = new ArrayList<>();

        public void setData(List<StudentService.GradeView> d) {
            data = d;
            fireTableDataChanged();
        }

        @Override public int getRowCount() {
            int count = 0;
            for (var g : data) {
                count += g.getAssessments().size();
            }
            return count;
        }

        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            int index = 0;
            for (var g : data) {
                for (Assessment a : g.getAssessments()) {
                    if (index == r) {
                        Score sc = g.getScores().stream()
                                .filter(s -> s.getAssessmentId() == a.getId())
                                .findFirst().orElse(null);

                        return switch (c) {
                            case 0 -> g.getSection().getSectionId();
                            case 1 -> a.getName();
                            case 2 -> sc == null ? "" : sc.getMarksObtained();
                            case 3 -> g.getFinalGrade().getPercentage();
                            default -> null;
                        };
                    }
                    index++;
                }
            }
            return null;
        }
    }
}
