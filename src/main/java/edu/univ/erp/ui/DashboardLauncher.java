package edu.univ.erp.ui;

import edu.univ.erp.domain.*;

public class DashboardLauncher {

    public static void launch(User user) {

        switch (user.getRole().toLowerCase()) {
            case "admin" -> new AdminDashboard((Admin) user).setVisible(true);
            case "student" -> new StudentDashboard((Student) user).setVisible(true);
            case "instructor" -> new InstructorDashboard((Instructor) user).setVisible(true);
            default -> throw new RuntimeException("Unknown role: " + user.getRole());
        }
    }
}
