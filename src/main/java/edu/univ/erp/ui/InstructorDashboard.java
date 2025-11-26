package edu.univ.erp.ui;

import edu.univ.erp.api.InstructorApi;
import edu.univ.erp.api.common.ApiResult;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class InstructorDashboard extends JFrame {

    private final Instructor instructorUser;
    private final InstructorApi api;

    // Left Navigation
    private JPanel navPanel;

    // Right "card" container
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Cards
    private JPanel sectionsCard;
    private JPanel assessmentsCard;
    private JPanel gradebookCard;
    private JPanel finalsCard;

    // UI State
    private List<Section> mySections = new ArrayList<>();
    private JComboBox<String> sectionDropdown;
    private Section selectedSection;

    // Tables
    private JTable assessmentsTable;
    private JTable rosterTable;
    private JTable gradebookTable;
    private JTable finalPreviewTable;

    private AssessmentTableModel assessmentModel;
    private RosterTableModel rosterModel;
    private GradebookTableModel gradebookModel;
    private FinalPreviewTableModel finalPreviewModel;


    public InstructorDashboard(Instructor instructorUser) {
        super("Instructor Dashboard - " + instructorUser.getUsername());
        this.instructorUser = instructorUser;
        this.api = new InstructorApi(UiContext.get().instructors());

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        buildUI();
        loadSections();
    }

    // ============================================================
    // Build UI Layout
    // ============================================================
    private void buildUI() {
        setLayout(new BorderLayout());

        // LEFT NAVIGATION
        navPanel = new JPanel();
        navPanel.setLayout(new GridLayout(10, 1, 0, 5));
        navPanel.setPreferredSize(new Dimension(220, 800));
        navPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        JButton sectionsBtn = createNavButton("My Sections");
        JButton assessBtn = createNavButton("Assessments");
        JButton gradebookBtn = createNavButton("Gradebook");
        JButton finalsBtn = createNavButton("Final Grades");

        sectionsBtn.addActionListener(e -> showCard("sections"));
        assessBtn.addActionListener(e -> showCard("assessments"));
        gradebookBtn.addActionListener(e -> showCard("gradebook"));
        finalsBtn.addActionListener(e -> showCard("finals"));

        navPanel.add(sectionsBtn);
        navPanel.add(assessBtn);
        navPanel.add(gradebookBtn);
        navPanel.add(finalsBtn);

        add(navPanel, BorderLayout.WEST);

        // RIGHT CONTENT
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        buildSectionsCard();
        buildAssessmentsCard();
        buildGradebookCard();
        buildFinalGradesCard();

        contentPanel.add(sectionsCard, "sections");
        contentPanel.add(assessmentsCard, "assessments");
        contentPanel.add(gradebookCard, "gradebook");
        contentPanel.add(finalsCard, "finals");

        add(contentPanel, BorderLayout.CENTER);
    }

    private JButton createNavButton(String text) {
        JButton b = new JButton(text);
        b.putClientProperty("JButton.buttonType", "borderless");
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(new Font("SansSerif", Font.PLAIN, 16));
        return b;
    }

    private void showCard(String name) {
        cardLayout.show(contentPanel, name);
    }

    // ============================================================
    // Sections Card
    // ============================================================
    private void buildSectionsCard() {
        sectionsCard = new JPanel(new BorderLayout());
        sectionsCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lbl = new JLabel("My Sections");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 22));

        sectionDropdown = new JComboBox<>();

        JPanel top = new JPanel(new BorderLayout());
        top.add(lbl, BorderLayout.WEST);
        top.add(sectionDropdown, BorderLayout.EAST);

        sectionsCard.add(top, BorderLayout.NORTH);

        JTextArea details = new JTextArea();
        details.setEditable(false);
        details.setFont(new Font("Monospaced", Font.PLAIN, 15));

        sectionsCard.add(new JScrollPane(details), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadSections());
        sectionsCard.add(refresh, BorderLayout.SOUTH);

        sectionDropdown.addActionListener(e -> showSectionDetails(details));
    }

    private void showSectionDetails(JTextArea details) {
        int idx = sectionDropdown.getSelectedIndex();
        if (idx < 0 || idx >= mySections.size()) return;

        selectedSection = mySections.get(idx);

        details.setText(
                "Section ID: " + selectedSection.getSectionId() + "\n" +
                        "Course ID: " + selectedSection.getCourseId() + "\n" +
                        "Instructor: " + selectedSection.getInstructorId() + "\n" +
                        "Time: " + selectedSection.getDayTime() + "\n" +
                        "Room: " + selectedSection.getRoom() + "\n" +
                        "Capacity: " + selectedSection.getCapacity() + "\n" +
                        "Semester: " + selectedSection.getSemester() + "\n" +
                        "Year: " + selectedSection.getYear()
        );
    }

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
            selectedSection = mySections.get(0);
        }
    }

    // ============================================================
    // Assessments Card
    // ============================================================
    private void buildAssessmentsCard() {
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
    }

    private void refreshAssessments() {
        if (selectedSection == null) return;

        ApiResult<List<Assessment>> r = api.listAssessments(selectedSection.getSectionId());
        if (!r.isSuccess()) {
            JOptionPane.showMessageDialog(this, r.getMessage());
            return;
        }

        assessmentModel.setData(r.getData());
    }

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

        int opt = JOptionPane.showConfirmDialog(this, form, "Add Assessment",
                JOptionPane.OK_CANCEL_OPTION);

        if (opt == JOptionPane.OK_OPTION) {
            try {
                Assessment a = new Assessment(
                        0,
                        selectedSection.getSectionId(),
                        name.getText(),
                        Double.parseDouble(maxMarks.getText()),
                        Double.parseDouble(weight.getText())
                );
                api.addAssessment(a);
                refreshAssessments();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
            }
        }
    }

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

        if (JOptionPane.showConfirmDialog(this, form,
                "Edit Assessment", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            try {
                Assessment updated = new Assessment(
                        a.getId(),
                        a.getSectionId(),
                        name.getText(),
                        Double.parseDouble(maxMarks.getText()),
                        Double.parseDouble(weight.getText())
                );

                api.updateAssessment(updated);
                refreshAssessments();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
            }
        }
    }

    private void deleteSelectedAssessment() {
        int row = assessmentsTable.getSelectedRow();
        if (row < 0) return;

        Assessment a = assessmentModel.get(row);

        if (JOptionPane.showConfirmDialog(this,
                "Delete assessment?", "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            api.deleteAssessment(a.getId());
            refreshAssessments();
        }
    }

    // ============================================================
    // Gradebook Card
    // ============================================================
    private void buildGradebookCard() {
        gradebookCard = new JPanel(new BorderLayout());
        gradebookCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        gradebookModel = new GradebookTableModel();
        gradebookTable = new JTable(gradebookModel);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton load = new JButton("Load Gradebook");
        JButton save = new JButton("Save Scores");

        top.add(load);
        top.add(save);

        load.addActionListener(e -> loadGradebook());
        save.addActionListener(e -> saveScoresFromGrid());

        gradebookCard.add(top, BorderLayout.NORTH);
        gradebookCard.add(new JScrollPane(gradebookTable), BorderLayout.CENTER);
    }

    private void loadGradebook() {
        if (selectedSection == null) return;

        ApiResult<InstructorService.SectionGradeSummary> r =
                api.getGradebook(selectedSection.getSectionId());

        if (!r.isSuccess()) {
            JOptionPane.showMessageDialog(this, r.getMessage());
            return;
        }

        gradebookModel.setData(r.getData());
    }

    private void saveScoresFromGrid() {
        List<Score> list = gradebookModel.extractScores();

        ApiResult<String> r = api.saveScores(list);
        JOptionPane.showMessageDialog(this, r.getMessage());
    }

    // ============================================================
    // Final Grades Card
    // ============================================================
    private void buildFinalGradesCard() {
        finalsCard = new JPanel(new BorderLayout());
        finalsCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        finalPreviewModel = new FinalPreviewTableModel();
        finalPreviewTable = new JTable(finalPreviewModel);

        JButton preview = new JButton("Preview Finals");
        JButton finalizeBtn = new JButton("Finalize Grades");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(preview);
        top.add(finalizeBtn);

        preview.addActionListener(e -> previewFinals());
        finalizeBtn.addActionListener(e -> finalizeGrades());

        finalsCard.add(top, BorderLayout.NORTH);
        finalsCard.add(new JScrollPane(finalPreviewTable), BorderLayout.CENTER);
    }

    private void previewFinals() {
        if (selectedSection == null) return;

        ApiResult<List<FinalGrade>> r =
                api.previewFinalGrades(selectedSection.getSectionId());

        if (!r.isSuccess()) {
            JOptionPane.showMessageDialog(this, r.getMessage());
            return;
        }

        finalPreviewModel.setData(r.getData());
    }

    private void finalizeGrades() {
        if (selectedSection == null) return;

        ApiResult<String> r =
                api.finalizeGrades(selectedSection.getSectionId());

        JOptionPane.showMessageDialog(this, r.getMessage());
    }

    // ============================================================
    // Table Models
    // ============================================================

    // ---------- ASSESSMENTS TABLE ----------
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

    // ---------- ROSTER ----------
    private static class RosterTableModel extends AbstractTableModel {

        private final String[] cols = {"Roll", "Name", "Program", "Year"};
        private List<Student> students = new ArrayList<>();

        public void setData(List<Student> list) {
            students = list;
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return students.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            Student s = students.get(r);
            return switch (c) {
                case 0 -> s.getRollNo();
                case 1 -> s.getUsername();
                case 2 -> s.getProgram();
                case 3 -> s.getYear();
                default -> null;
            };
        }
    }

    // ---------- GRADEBOOK TABLE ----------
    private static class GradebookTableModel extends AbstractTableModel {

        private List<Assessment> assessments = new ArrayList<>();
        private List<InstructorService.StudentGradeRow> rows = new ArrayList<>();
        private String[] colNames;

        public void setData(InstructorService.SectionGradeSummary summary) {
            this.assessments = summary.getAssessments();
            this.rows = summary.getRows();

            colNames = new String[assessments.size() + 3];
            colNames[0] = "Roll";
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
            if (c == 1) return row.getStudent().getUsername();

            int assessIndex = c - 2;

            if (assessIndex >= 0 && assessIndex < assessments.size()) {
                Assessment a = assessments.get(assessIndex);

                Score score = row.getScores().stream()
                        .filter(s -> s.getAssessmentId() == a.getId())
                        .findFirst()
                        .orElse(null);

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

                // Replace or add score
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

    // ---------- FINAL PREVIEW TABLE ----------
    private static class FinalPreviewTableModel extends AbstractTableModel {

        private List<FinalGrade> data = new ArrayList<>();

        @Override
        public int getRowCount() { return data.size(); }

        @Override
        public int getColumnCount() { return 3; }

        @Override
        public String getColumnName(int c) {
            return switch (c) {
                case 0 -> "Student ID";
                case 1 -> "Percentage";
                case 2 -> "Grade";
                default -> "";
            };
        }

        @Override
        public Object getValueAt(int r, int c) {
            FinalGrade fg = data.get(r);
            return switch (c) {
                case 0 -> fg.getStudentId();
                case 1 -> fg.getPercentage();
                case 2 -> fg.getLetter();
                default -> null;
            };
        }

        public void setData(List<FinalGrade> list) {
            data = list;
            fireTableDataChanged();
        }
    }
}
