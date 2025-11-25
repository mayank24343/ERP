package edu.univ.erp.access;

import edu.univ.erp.domain.User;

public class CurrentSession {

    private static User currentUser;

    public static synchronized void set(User user) {
        currentUser = user;
    }

    public static synchronized User get() {
        return currentUser;
    }

    public static synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public static synchronized void clear() {
        currentUser = null;
    }
}
