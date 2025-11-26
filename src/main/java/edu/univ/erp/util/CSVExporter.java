package edu.univ.erp.util;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.FileWriter;
import java.io.IOException;

public class CSVExporter {

    public static boolean exportTable(JTable table, String defaultName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(defaultName + ".csv"));

        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();

            try (FileWriter fw = new FileWriter(file)) {
                TableModel model = table.getModel();

                // Write header row
                for (int col = 0; col < model.getColumnCount(); col++) {
                    fw.write(model.getColumnName(col));
                    if (col < model.getColumnCount() - 1) fw.write(",");
                }
                fw.write("\n");

                // Write data rows
                for (int row = 0; row < model.getRowCount(); row++) {
                    for (int col = 0; col < model.getColumnCount(); col++) {
                        Object value = model.getValueAt(row, col);
                        fw.write(value == null ? "" : value.toString());
                        if (col < model.getColumnCount() - 1) fw.write(",");
                    }
                    fw.write("\n");
                }

                JOptionPane.showMessageDialog(null, "CSV exported successfully!");
                return true;

            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error exporting CSV: " + e.getMessage());
                return false;
            }
        }

        return false;
    }
}
