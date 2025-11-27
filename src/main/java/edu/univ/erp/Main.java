package edu.univ.erp;

import edu.univ.erp.ui.LoginWindow;
import edu.univ.erp.ui.UiContext;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Swing UI must run on EDT
        SwingUtilities.invokeLater(() -> {
            UiContext ctx = UiContext.get();

            LoginWindow login = new LoginWindow(ctx.auth(), ctx.users());
            login.setVisible(true);
        });
    }
}

