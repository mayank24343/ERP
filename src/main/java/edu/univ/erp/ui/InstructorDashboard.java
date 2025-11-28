package edu.univ.erp.ui;

import edu.univ.erp.api.auth.AuthApi;
import edu.univ.erp.api.instructor.InstructorApi;
import edu.univ.erp.api.common.ApiResult;
import edu.univ.erp.api.maintenance.MaintenanceApi;
import edu.univ.erp.api.slabs.GradeSlabApi;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.util.CSVExporter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstructorDashboard extends JFrame {
    private final Instructor instructorUser;
    private final InstructorApi api;
    private final MaintenanceApi maintenanceApi;
    private final AuthApi authApi;
    private final GradeSlabApi  slabApi;
    //Main structure
    private JPanel navPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    //cards for card layout
    private JPanel sectionsCard;
    private JPanel assessmentsCard;
    private JPanel gradebookCard;
    private JPanel finalsCard;
    private JPanel statsCard;
    private JPanel slabsCard;
    private JPanel statsContent;
    //the selected section determines output of assessments, grades etc.
    private List<Section> mySections = new ArrayList<>();
    private JComboBox<String> sectionDropdown;
    private Section selectedSection;
    //Tables
    private JTable assessmentsTable;
    private JTable gradebookTable;
    private JTable finalPreviewTable;
    private JTable slabTable;
    //Models for table
    private AssessmentTableModel assessmentModel;
    private GradebookTableModel gradebookModel;
    private FinalPreviewTableModel finalPreviewModel;
    private SlabModel slabModel;
    //IIIT Delhi color for styling
    private final Color teal = new Color(63, 173, 168);
    //constructor
    public InstructorDashboard(Instructor instructorUser) {
        super("University ERP | Instructor Dashboard - " + instructorUser.getUsername());
        this.instructorUser = instructorUser;
        this.api = new InstructorApi(UiContext.get().instructors());
        this.authApi = new AuthApi(UiContext.get().auth());
        this.maintenanceApi = new MaintenanceApi();
        this.slabApi = new GradeSlabApi(UiContext.get().slabs());

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        initUI();
        loadSections();
    }

    //ui initialisation
    private void initUI() {
        setLayout(new BorderLayout());
        //find if maintenance is on
        boolean maintenanceOn = maintenanceApi.isMaintenanceOn().getData();

        //left nav
        navPanel = new JPanel();
        navPanel.setLayout(new GridLayout(10, 1, 0, 5));
        navPanel.setPreferredSize(new Dimension(220, 800));
        navPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        navPanel.setBackground(teal);
        JButton sectionsBtn = createNavButton("My Sections");
        JButton assessBtn = createNavButton("Assessments");
        JButton gradebookBtn = createNavButton("Gradebook");
        JButton finalsBtn = createNavButton("Final Grades");
        JButton statsBtn = createNavButton("Section Stats");
        JButton slabsBtn = createNavButton("Grading Slabs");
        slabsBtn.addActionListener(e -> showCard("slabs"));
        sectionsBtn.addActionListener(e -> showCard("sections"));
        assessBtn.addActionListener(e -> showCard("assessments"));
        gradebookBtn.addActionListener(e -> showCard("gradebook"));
        finalsBtn.addActionListener(e -> showCard("finals"));
        statsBtn.addActionListener(e -> showCard("stats"));
        navPanel.add(sectionsBtn);
        navPanel.add(assessBtn);
        navPanel.add(gradebookBtn);
        navPanel.add(finalsBtn);
        navPanel.add(statsBtn);
        navPanel.add(slabsBtn);

        //cards layout to switch screens for functionality
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        //build cards
        initSectionsCard();
        initAssessmentsCard();
        initGradebookCard();
        initFinalGradesCard();
        initStatsCard();
        initSlabsCard();

        //add them to cards layout
        contentPanel.add(sectionsCard, "sections");
        contentPanel.add(assessmentsCard, "assessments");
        contentPanel.add(gradebookCard, "gradebook");
        contentPanel.add(finalsCard, "finals");
        contentPanel.add(statsCard, "stats");
        contentPanel.add(slabsCard, "slabs");

        add(createTopBar(instructorUser.getFullname()), BorderLayout.NORTH); //top bar with welcome, logout, change password
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
                    instructorUser.getUsername(),
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

    //instructor sections detail & dropdown choose
    private void initSectionsCard() {
        sectionsCard = new JPanel(new BorderLayout());
        sectionsCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel lbl = new JLabel("My Sections");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        sectionDropdown = new JComboBox<>();

        JPanel top = new JPanel(new BorderLayout());
        top.add(lbl, BorderLayout.WEST);
        top.add(sectionDropdown, BorderLayout.EAST);

        JTextArea details = new JTextArea();
        details.setEditable(false);
        details.setFont(new Font("Monospaced", Font.PLAIN, 15));
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadSections());

        sectionDropdown.addActionListener(e -> showSectionDetails(details));

        sectionsCard.add(top, BorderLayout.NORTH);
        sectionsCard.add(refresh, BorderLayout.SOUTH);
        sectionsCard.add(new JScrollPane(details), BorderLayout.CENTER);
    }

    //display section details
    private void showSectionDetails(JTextArea details) {
        int idx = sectionDropdown.getSelectedIndex();
        if (idx < 0 || idx >= mySections.size()) return;
        selectedSection = mySections.get(idx);

        details.setText("Section ID: " + selectedSection.getSectionId() + "\n" +
                        "Course Code: " + selectedSection.getCourse().getCode() + "\n" +
                        "Course Title: " + selectedSection.getCourse().getTitle() + "\n" +
                        "Instructor: " + selectedSection.getInstructor().getFullname() + "\n" +
                        "Time: " + selectedSection.getDayTime() + "\n" +
                        "Room: " + selectedSection.getRoom() + "\n" +
                        "Capacity: " + selectedSection.getCapacity() + "\n" +
                        "Semester: " + selectedSection.getSemester() + "\n" +
                        "Year: " + selectedSection.getYear());
        details.setFont(new Font("SansSerif", Font.BOLD, 15));
    }

    //load sections for instructor
    private void loadSections() {
        ApiResult<List<Section>> r = api.getMySections(instructorUser.getUserId());
        if (!r.isSuccess()) {
            JOptionPane.showMessageDialog(this, r.getMessage());
            return;
        }

        mySections = r.getData();
        sectionDropdown.removeAllItems();
        for (Section s : mySections) {
            sectionDropdown.addItem(
                    "Section " + s.getSectionId() + " - " + s.getDayTime()
            );
        }

        if (!mySections.isEmpty()) {
            selectedSection = mySections.getFirst();
        }
    }

    //show assessments for section
    private void initAssessmentsCard() {
        assessmentsCard = new JPanel(new BorderLayout());
        assessmentsCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        assessmentModel = new AssessmentTableModel();
        assessmentsTable = new JTable(assessmentModel);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton loadBtn = new JButton("Load");
        btnBar.add(loadBtn);
        btnBar.add(addBtn);
        btnBar.add(editBtn);
        btnBar.add(deleteBtn);
        loadBtn.addActionListener(e -> refreshAssessments());
        addBtn.addActionListener(e -> openAddAssessmentDialog());
        editBtn.addActionListener(e -> openEditAssessmentDialog());
        deleteBtn.addActionListener(e -> deleteSelectedAssessment());

        assessmentsCard.add(btnBar, BorderLayout.NORTH);
        assessmentsCard.add(new JScrollPane(assessmentsTable), BorderLayout.CENTER);
        refreshAssessments();
    }

    //refresh the loaded assessments to see newly created ones
    private void refreshAssessments() {
        if (selectedSection == null) return;

        ApiResult<List<Assessment>> r = api.listAssessments(selectedSection.getSectionId(), instructorUser.getUserId());
        if (!r.isSuccess()) {
            JOptionPane.showMessageDialog(this, r.getMessage());
            return;
        }

        assessmentModel.setData(r.getData());
    }

    //create new assessment
    private void openAddAssessmentDialog() {
        if (selectedSection == null) return;
        JTextField name = new JTextField();
        JTextField maxMarks = new JTextField();
        JTextField weight = new JTextField();
        Object[] form = {
                "Name:", name,
                "Max Marks:", maxMarks,
                "Weight (%):", weight
        };

        int opt = JOptionPane.showConfirmDialog(this, form, "Add Assessment", JOptionPane.OK_CANCEL_OPTION);

        if (opt == JOptionPane.OK_OPTION) {
            try {
                Assessment a = new Assessment(0, selectedSection.getSectionId(), name.getText(), Double.parseDouble(maxMarks.getText()), Double.parseDouble(weight.getText()));
                var r = api.addAssessment(a, instructorUser.getUserId());
                JOptionPane.showMessageDialog(this, r.getMessage());
                refreshAssessments();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
            }
        }
    }

    //edit assessment
    private void openEditAssessmentDialog() {
        int row = assessmentsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an assessment!");
            return;
        }
        Assessment a = assessmentModel.get(row);

        JTextField name = new JTextField(a.getName());
        JTextField maxMarks = new JTextField(String.valueOf(a.getMaxMarks()));
        JTextField weight = new JTextField(String.valueOf(a.getWeight()));
        Object[] form = {
                "Name:", name,
                "Max Marks:", maxMarks,
                "Weight (%):", weight
        };

        if (JOptionPane.showConfirmDialog(this, form, "Edit Assessment", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            try {
                Assessment updated = new Assessment(a.getId(), a.getSectionId(), name.getText(), Double.parseDouble(maxMarks.getText()), Double.parseDouble(weight.getText()));
                var r = api.updateAssessment(updated, instructorUser.getUserId());
                refreshAssessments();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
            }
        }
    }

    //delete assessment
    private void deleteSelectedAssessment() {
        int row = assessmentsTable.getSelectedRow();
        if (row < 0) return;
        Assessment a = assessmentModel.get(row);

        if (JOptionPane.showConfirmDialog(this, "Delete assessment?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            api.deleteAssessment(a, instructorUser.getUserId());
            refreshAssessments();
        }
    }

    //section component grades
    private void initGradebookCard() {
        gradebookCard = new JPanel(new BorderLayout());
        gradebookCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        gradebookModel = new GradebookTableModel();
        gradebookTable = new JTable(gradebookModel);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton load = new JButton("Load Gradebook");
        JButton save = new JButton("Save Scores");
        JButton exportFinals = new JButton("Export Final Grades");
        exportFinals.addActionListener(e -> CSVExporter.exportTable(gradebookTable,
                "my_final_grades"));

        //sorting
        String[] sortOptions = {
                "Final %",
                "Roll No",
                "Name",
        };
        JComboBox<String> sortBox = new JComboBox<>(sortOptions);
        gradebookTable.setAutoCreateRowSorter(true);
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) gradebookTable.getRowSorter();
        sortBox.addActionListener(e -> {
            int col = switch (sortBox.getSelectedIndex()) {
                case 0 -> gradebookModel.getColumnCount() - 1;
                case 1 -> 0;
                case 2 -> 1;
                default -> 0;
            };

            sorter.setSortKeys(List.of(new RowSorter.SortKey(col, SortOrder.ASCENDING)));
        });

        top.add(new JLabel("Sort by:"));
        top.add(sortBox);
        top.add(load);
        top.add(save);
        top.add(exportFinals);

        load.addActionListener(e -> loadGradebook());
        save.addActionListener(e -> saveScoresFromGrid());

        gradebookCard.add(top, BorderLayout.NORTH);
        gradebookCard.add(new JScrollPane(gradebookTable), BorderLayout.CENTER);
    }

    //load grades for section
    private void loadGradebook() {
        if (selectedSection == null) return;
        var r = api.getGradebook(selectedSection.getSectionId(), instructorUser.getUserId());

        if (!r.isSuccess()) {
            JOptionPane.showMessageDialog(this, r.getMessage());
            return;
        }

        gradebookModel.setData(r.getData());
    }

    //save edited scores
    private void saveScoresFromGrid() {
        List<Score> list = gradebookModel.extractScores();
        var r = api.saveScores(list, instructorUser.getUserId(), selectedSection.getSectionId());
        JOptionPane.showMessageDialog(this, r.getMessage());
    }

    //section final grades
    private void initFinalGradesCard() {
        finalsCard = new JPanel(new BorderLayout());
        finalsCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        finalPreviewModel = new FinalPreviewTableModel();
        finalPreviewTable = new JTable(finalPreviewModel);

        //sorting
        String[] sortOptions = {
                "Student",
                "Roll No",
                "Final %",
                "Grade",
        };
        JComboBox<String> sortBox = new JComboBox<>(sortOptions);
        finalPreviewTable.setAutoCreateRowSorter(true);
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) finalPreviewTable.getRowSorter();
        sortBox.addActionListener(e -> {
            int col = switch (sortBox.getSelectedIndex()) {
                case 0 -> 0;
                case 1 -> 1;
                case 2 -> 2;
                case 3 -> 3;
                default -> 0;
            };
            sorter.setSortKeys(List.of(new RowSorter.SortKey(col, SortOrder.ASCENDING)));
        });

        JButton preview = new JButton("Preview Finals");
        JButton finalizeBtn = new JButton("Finalize Grades");
        JButton exportFinals = new JButton("Export Final Grades");
        exportFinals.addActionListener(e -> CSVExporter.exportTable(finalPreviewTable, "my_final_grades"));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Sort by:"));
        top.add(sortBox);
        top.add(preview);
        top.add(finalizeBtn);
        top.add(exportFinals);

        preview.addActionListener(e -> previewFinals());
        finalizeBtn.addActionListener(e -> finalizeGrades());
        finalsCard.add(top, BorderLayout.NORTH);
        finalsCard.add(new JScrollPane(finalPreviewTable), BorderLayout.CENTER);
    }

    //see the to be grades
    private void previewFinals() {
        if (selectedSection == null) return;
        ApiResult<List<FinalGrade>> r = api.previewFinalGrades(selectedSection.getSectionId(), instructorUser.getUserId());

        if (!r.isSuccess()) {
            JOptionPane.showMessageDialog(this, r.getMessage());
            return;
        }

        finalPreviewModel.setData(r.getData());
    }

    //save final grades
    private void finalizeGrades() {
        if (selectedSection == null) return;
        var r = api.finalizeGrades(selectedSection.getSectionId(), instructorUser.getUserId());
        JOptionPane.showMessageDialog(this, r.getMessage());
    }

    //statistics card
    private void initStatsCard() {
        statsCard = new JPanel(new BorderLayout());
        statsCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        //load stats button panel
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadBtn = new JButton("Load Statistics");
        top.add(loadBtn);
        loadBtn.addActionListener(e -> loadStatisticsForSelected());
        statsCard.add(top, BorderLayout.NORTH);

        //stats
        statsContent = new JPanel(new BorderLayout());
        statsContent.add(new JLabel("Click 'Load Statistics' to view charts.", SwingConstants.CENTER), BorderLayout.CENTER);
        statsCard.add(statsContent, BorderLayout.CENTER);
    }

    //all stats
    private void loadStatisticsForSelected() {
        if (selectedSection == null) return;
        //grades for section
        ApiResult<InstructorService.SectionGradeSummary> r = api.getGradebook(selectedSection.getSectionId(), instructorUser.getUserId());
        if (!r.isSuccess()) {
            statsCard.add(new JLabel("Unable to load statistics."), BorderLayout.CENTER);
            return;
        }

        var summary = r.getData();
        //get all final grades
        List<FinalGrade> finals = summary.getRows().stream().map(InstructorService.StudentGradeRow::getFinalGrade).filter(f -> f != null).toList();
        //stats
        double mean = finals.stream().mapToDouble(FinalGrade::getPercentage).average().orElse(0);
        double min = finals.stream().mapToDouble(FinalGrade::getPercentage).min().orElse(0);
        double max = finals.stream().mapToDouble(FinalGrade::getPercentage).max().orElse(0);
        double median = 0;
        if (!finals.isEmpty()) {
            List<Double> sorted = finals.stream()
                    .map(FinalGrade::getPercentage)
                    .sorted()
                    .toList();
            int mid = sorted.size() / 2;
            median = sorted.size() % 2 == 0
                    ? (sorted.get(mid) + sorted.get(mid - 1)) / 2.0
                    : sorted.get(mid);
        }

        double sd = Math.sqrt(finals.stream().mapToDouble(f -> Math.pow(f.getPercentage() - mean, 2)).average().orElse(0));
        //summary panel
        JPanel statsPanel = new JPanel(new GridLayout(5, 1));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        statsPanel.add(new JLabel("Mean: " + mean));
        statsPanel.add(new JLabel("Median: " + median));
        statsPanel.add(new JLabel("Min: " + min));
        statsPanel.add(new JLabel("Max: " + max));
        statsPanel.add(new JLabel("Std Dev: " + sd));

        DefaultCategoryDataset gradeDataset = new DefaultCategoryDataset();
        Map<String, Integer> gradeCount = new HashMap<>();
        for (FinalGrade f : finals) {
            gradeCount.merge(f.getLetter(), 1, Integer::sum);
        }
        for (var entry : gradeCount.entrySet()) {
            gradeDataset.addValue(entry.getValue(), "Count", entry.getKey());
        }

        JFreeChart gradeChart = ChartFactory.createBarChart("Grade Distribution", "Grade", "Count", gradeDataset);
        //avg scores
        DefaultCategoryDataset assessmentDataset = new DefaultCategoryDataset();
        for (Assessment a : summary.getAssessments()) {
            double avg = summary.getRows().stream().flatMap(row -> row.getScores().stream()).filter(s -> s.getAssessmentId() == a.getId()).mapToDouble(Score::getMarksObtained).average().orElse(0);
            double pct = a.getMaxMarks() > 0 ? (avg / a.getMaxMarks()) * 100 : 0;
            assessmentDataset.addValue(pct, "Percentage", a.getName());
        }

        JFreeChart avgChart = ChartFactory.createBarChart("Assessment Averages (%)", "Assessment", "Avg %", assessmentDataset);

        //graphs
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2));
        chartsPanel.add(new ChartPanel(gradeChart));
        chartsPanel.add(new ChartPanel(avgChart));

        //grpahs and summary
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                chartsPanel,
                statsPanel
        );
        split.setDividerLocation(700); // adjust as needed

        //replace content in card
        statsCard.remove(statsContent);
        statsContent.removeAll();
        statsContent.add(split);
        statsCard.add(statsContent, BorderLayout.CENTER);
        statsCard.revalidate();
        statsCard.repaint();
    }

    //slabs edit
    private void initSlabsCard() {
        slabsCard = new JPanel(new BorderLayout());
        slabsCard.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        slabTable = new JTable(slabModel = new SlabModel());
        slabsCard.add(new JScrollPane(slabTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton add = new JButton("Add Slab");
        JButton edit = new JButton("Edit");
        JButton delete = new JButton("Delete");
        JButton load = new JButton("Load");
        buttons.add(load);
        buttons.add(add);
        buttons.add(edit);
        buttons.add(delete);
        load.addActionListener(e -> loadSlabs());
        add.addActionListener(e -> openAddSlabDialog());
        edit.addActionListener(e -> openEditSlabDialog());
        delete.addActionListener(e -> deleteSlab());

        slabsCard.add(buttons, BorderLayout.NORTH);
    }

    //load slabs for section
    private void loadSlabs() {
        var r = slabApi.list(selectedSection.getSectionId(), instructorUser.getUserId());
        if (!r.isSuccess()) {
            JOptionPane.showMessageDialog(this, r.getMessage());
            return;
        }
        slabModel.setData(r.getData());
    }

    //add slabs
    private void openAddSlabDialog() {
        JTextField letter = new JTextField();
        JTextField min = new JTextField();
        JTextField max = new JTextField();
        Object[] form = {
                "Letter Grade:", letter,
                "Min %:", min,
                "Max %:", max
        };

        if (JOptionPane.showConfirmDialog(this, form, "Add Grade Slab", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            try {
                String L = letter.getText().trim();
                double lo = Double.parseDouble(min.getText().trim());
                double hi = Double.parseDouble(max.getText().trim());
                validateSlab(L, lo, hi);
                var r = slabApi.add(selectedSection.getSectionId(), L, lo, hi, instructorUser.getUserId());

                JOptionPane.showMessageDialog(this, r.getMessage());
                loadSlabs();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    //edit slabs
    private void openEditSlabDialog() {
        int view = slabTable.getSelectedRow();
        if (view < 0) {
            JOptionPane.showMessageDialog(this, "Select a slab first");
            return;
        }

        int row = slabTable.convertRowIndexToModel(view);
        GradeSlab g = slabModel.get(row);

        JTextField letter = new JTextField(g.getLetter());
        JTextField min = new JTextField(String.valueOf(g.getMin()));
        JTextField max = new JTextField(String.valueOf(g.getMax()));

        Object[] form = {
                "Letter Grade:", letter,
                "Min %:", min,
                "Max %:", max
        };

        if (JOptionPane.showConfirmDialog(this, form, "Edit Slab",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            try {
                String L = letter.getText().trim();
                double lo = Double.parseDouble(min.getText().trim());
                double hi = Double.parseDouble(max.getText().trim());

                validateSlab(L, lo, hi);
                GradeSlab updated = new GradeSlab(g.getId(),selectedSection.getSectionId(), L, lo, hi);
                ApiResult<String> r = slabApi.update(updated, selectedSection.getSectionId(), instructorUser.getUserId());

                JOptionPane.showMessageDialog(this, r.getMessage());
                loadSlabs();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    //delete slabs
    private void deleteSlab() {
        int view = slabTable.getSelectedRow();
        if (view < 0) {
            JOptionPane.showMessageDialog(this, "Select a slab first");
            return;
        }

        int row = slabTable.convertRowIndexToModel(view);
        GradeSlab g = slabModel.get(row);
        if (JOptionPane.showConfirmDialog(this,
                "Delete slab \"" + g.getLetter() + "\"?",
                "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            ApiResult<String> r = slabApi.delete(g.getId(), selectedSection.getSectionId(), instructorUser.getUserId());
            JOptionPane.showMessageDialog(this, r.getMessage());
            loadSlabs();
        }
    }

    //validate slabs
    private void validateSlab(String letter, double min, double max) throws IllegalArgumentException {
        if (letter.isEmpty())
            throw new IllegalArgumentException("Letter cannot be empty");
        if (min >= max)
            throw new IllegalArgumentException("Min must be less than Max");
    }

    //models for table
    private static class AssessmentTableModel extends AbstractTableModel {
        private final String[] cols = {"ID", "Name", "Max", "Weight"};
        private List<Assessment> data = new ArrayList<>();
        public void setData(List<Assessment> list) {
            data = list;
            fireTableDataChanged();
        }

        public Assessment get(int row) { return data.get(row); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            Assessment a = data.get(r);
            return switch (c) {
                case 0 -> a.getId();
                case 1 -> a.getName();
                case 2 -> a.getMaxMarks();
                case 3 -> a.getWeight();
                default -> null;
            };
        }
    }

    private static class GradebookTableModel extends AbstractTableModel {

        private List<Assessment> assessments = new ArrayList<>();
        private List<InstructorService.StudentGradeRow> rows = new ArrayList<>();
        private String[] colNames;

        public void setData(InstructorService.SectionGradeSummary summary) {
            this.assessments = summary.getAssessments();
            this.rows = summary.getRows();
            colNames = new String[assessments.size() + 3];
            colNames[0] = "Roll No";
            colNames[1] = "Name";

            for (int i = 0; i < assessments.size(); i++) {
                colNames[i + 2] = assessments.get(i).getName();
            }

            colNames[colNames.length - 1] = "Final %";

            fireTableStructureChanged();
        }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return colNames == null ? 0 : colNames.length; }
        @Override public String getColumnName(int c) { return colNames[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            InstructorService.StudentGradeRow row = rows.get(r);
            if (c == 0) return row.getStudent().getRollNo();
            if (c == 1) return row.getStudent().getFullname();

            int assessIndex = c - 2;
            if (assessIndex >= 0 && assessIndex < assessments.size()) {
                Assessment a = assessments.get(assessIndex);
                Score score = row.getScores().stream().filter(s -> s.getAssessmentId() == a.getId()).findFirst().orElse(null);
                return score == null ? "" : score.getMarksObtained();
            }

            return row.getFinalGrade() == null ? "" : row.getFinalGrade().getPercentage();
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            // Only assessment columns editable
            return c >= 2 && c < colNames.length - 1;
        }

        @Override
        public void setValueAt(Object val, int r, int c) {
            try {
                double d = Double.parseDouble(val.toString());
                InstructorService.StudentGradeRow row = rows.get(r);
                Assessment a = assessments.get(c - 2);
                Score updated = new Score(a.getId(), row.getStudent().getUserId(), d);

                //replace or add score
                List<Score> newScores = new ArrayList<>(row.getScores());
                newScores.removeIf(s -> s.getAssessmentId() == a.getId());
                newScores.add(updated);

                row.getScores().clear();
                row.getScores().addAll(newScores);
                fireTableCellUpdated(r, c);
            } catch (Exception ignored) {}
        }

        public List<Score> extractScores() {
            List<Score> all = new ArrayList<>();
            for (InstructorService.StudentGradeRow row : rows) {
                all.addAll(row.getScores());
            }
            return all;
        }
    }

    private static class FinalPreviewTableModel extends AbstractTableModel {
        private List<FinalGrade> data = new ArrayList<>();

        @Override
        public int getRowCount() { return data.size(); }

        @Override
        public int getColumnCount() { return 4; }

        @Override
        public String getColumnName(int c) {
            return switch (c) {
                case 0 -> "Student";
                case 1 -> "Roll Number";
                case 2 -> "Percentage";
                case 3 -> "Grade";
                default -> "";
            };
        }

        @Override
        public Object getValueAt(int r, int c) {
            FinalGrade fg = data.get(r);
            return switch (c) {
                case 0 -> fg.getStudent().getFullname();
                case 1 -> fg.getStudent().getRollNo();
                case 2 -> fg.getPercentage();
                case 3 -> fg.getLetter();
                default -> null;
            };
        }

        public void setData(List<FinalGrade> list) {
            data = list;
            fireTableDataChanged();
        }
    }

    private static class SlabModel extends AbstractTableModel {
        private final String[] cols = {"Letter", "Min %", "Max %"};
        private List<GradeSlab> data = new ArrayList<>();

        public void setData(List<GradeSlab> d) {
            data = d;
            fireTableDataChanged();
        }

        public GradeSlab get(int row) { return data.get(row); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            GradeSlab g = data.get(r);
            return switch (c) {
                case 0 -> g.getLetter();
                case 1 -> g.getMin();
                case 2 -> g.getMax();
                default -> "";
            };
        }
    }
}