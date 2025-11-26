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
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDashboard extends JFrame {

    private final Student studentUser;

    //apis needed
    private final StudentApi api;
    private final AuthApi authApi;
    private final MaintenanceApi maintenanceApi;

    //
    private JPanel navPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    //functionality cards
    private JPanel catalogCard;
    private JPanel mySectionsCard;
    private JPanel registerCard;
    private JPanel timetableCard;
    private JPanel gradesCard;

    //tables
    private JTable catalogTable;
    private JTable mySectionsTable;
    private JTable finalGradesTable;
    private JTable breakdownTable;
    private JTable availableSectionsTable;

    //models for table
    private CatalogTableModel catalogModel;
    private MySectionsTableModel mySectionsModel;
    private FinalGradesTableModel finalGradesModel;
    private BreakdownTableModel breakdownModel;
    private AvailableSectionsTableModel availableSectionsModel;

    //
    private JComboBox<String> courseDropdown;

    //IIIT Delhi color for styling
    private final Color teal = new Color(63, 173, 168);

    //constructor
    public StudentDashboard(Student studentUser) {
        super("University ERP | Student Dashboard - " + studentUser.getFullname());
        this.studentUser = studentUser;
        this.api = new StudentApi(UiContext.get().students());
        this.authApi = new AuthApi(UiContext.get().auth());
        this.maintenanceApi = new MaintenanceApi();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        //initialize ui
        initUI();
        //first ui screen is course catalog, so load that
        loadCatalog();
    }

    //ui initialisation
    private void initUI() {
        setLayout(new BorderLayout());

        //find if maintenance is on
        boolean maintenanceOn = maintenanceApi.isMaintenanceOn().getData();

        //left hand side navigation
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

        //card layout for the functionality screens
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        //create the ui for the cards
        initCatalogCard();
        initMySectionsCard();
        initRegisterCard();
        initTimetableCard();
        initGradesCard();

        //add them to the content panel
        //by default catalog will be shown
        contentPanel.add(catalogCard, "catalog");
        contentPanel.add(mySectionsCard, "mysections");
        contentPanel.add(registerCard, "register");
        contentPanel.add(timetableCard, "timetable");
        contentPanel.add(gradesCard, "grades");

        add(createTopBar(studentUser.getFullname()), BorderLayout.NORTH); //top bar with welcome, logout, change password
        add(buildMaintenanceBanner(maintenanceOn), BorderLayout.AFTER_LAST_LINE);// maintenance flag
        add(navPanel, BorderLayout.WEST);//left side nav
        add(contentPanel, BorderLayout.CENTER);//main content
    }

    //maintenance flag
    private JPanel buildMaintenanceBanner(boolean maintenanceOn) {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setPreferredSize(new Dimension(1200, 40));

        if (!maintenanceOn) {
            banner.setVisible(false);
        }
        else{
            banner.setVisible(true);
            banner.setBackground(new Color(230, 160, 0)); // orange warning

            JLabel label = new JLabel(" Maintenance Mode On: View Only ");
            label.setForeground(Color.WHITE);
            label.setFont(new Font("SansSerif", Font.BOLD, 15));

            banner.add(label, BorderLayout.CENTER);
        }
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
                    studentUser.getUsername(),
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

    //course catalog card
    private void initCatalogCard() {
        catalogCard = new JPanel(new BorderLayout());
        catalogCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        catalogModel = new CatalogTableModel();
        catalogTable = new JTable(catalogModel);

        //for sorting the table
        String[] sortOptions = {
                "Course ID",
                "Code",
                "Title",
                "Credits"
        };
        JComboBox<String> sortBox = new JComboBox<>(sortOptions);
        catalogTable.setAutoCreateRowSorter(true);
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) catalogTable.getRowSorter();
        sortBox.addActionListener(e -> {
            int col = switch (sortBox.getSelectedIndex()) {
                case 0 -> 0;  // Course ID
                case 1 -> 1;  // Code
                case 2 -> 2;  // Title
                case 3 -> 3;  // Credits
                default -> 0;
            };

            sorter.setSortKeys(List.of(new RowSorter.SortKey(col, SortOrder.ASCENDING)));
        });

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadCatalog());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refresh);
        top.add(new JLabel("Sort by:"));
        top.add(sortBox);

        catalogCard.add(top, BorderLayout.NORTH);
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
    private void initMySectionsCard() {
        mySectionsCard = new JPanel(new BorderLayout());
        mySectionsCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        mySectionsModel = new MySectionsTableModel();
        mySectionsTable = new JTable(mySectionsModel);

        String[] sortOptions = {
                "Section ID",
                "Course",
                "Day/Time",
                "Room"
        };
        JComboBox<String> sortBox = new JComboBox<>(sortOptions);
        mySectionsTable.setAutoCreateRowSorter(true);
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) mySectionsTable.getRowSorter();
        sortBox.addActionListener(e -> {
            int col = switch (sortBox.getSelectedIndex()) {
                case 0 -> 0;  // Course ID
                case 1 -> 1;  // Code
                case 2 -> 2;  // Title
                case 3 -> 3;  // Credits
                default -> 0;
            };

            sorter.setSortKeys(List.of(new RowSorter.SortKey(col, SortOrder.ASCENDING)));
        });

        JButton refresh = new JButton("Refresh");
        JButton drop = new JButton("Drop Section");

        refresh.addActionListener(e -> loadMySections());
        drop.addActionListener(e -> dropSelectedSection());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refresh);
        top.add(drop);
        top.add(new JLabel("Sort by:"));
        top.add(sortBox);

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
    private void initRegisterCard() {
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

        //sorting sections
        String[] sortOptions = {
                "Section ID",
                "Instructor",
                "Time",
                "Room",
                "Capacity"
        };
        JComboBox<String> sortBox = new JComboBox<>(sortOptions);
        mySectionsTable.setAutoCreateRowSorter(true);
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) mySectionsTable.getRowSorter();
        sortBox.addActionListener(e -> {
            int col = switch (sortBox.getSelectedIndex()) {
                case 0 -> 0;
                case 1 -> 1;
                case 2 -> 2;
                case 3 -> 3;
                case 4 -> 4;
                default -> 0;
            };

            sorter.setSortKeys(List.of(new RowSorter.SortKey(col, SortOrder.ASCENDING)));
        });


        top.add(new JLabel("Sort by:"));
        top.add(sortBox);

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
    private void initTimetableCard() {
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
    private void initGradesCard() {
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

    //models for tables
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
        private final String[] cols = {"Section ID", "Course", "Day/Time", "Room"};
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
                case 1 -> s.getCourse().getTitle();
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
                case 1 -> s.getInstructor().getFullname();
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
                case 0 -> x.getSection().getCourse().getTitle();
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
                            case 0 -> g.getSection().getCourse().getTitle();
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
