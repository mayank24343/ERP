package edu.univ.erp.access;

import edu.univ.erp.domain.User;

public class CurrentSession {
    private static User currentUser;
    //user of the app setting, getting and removing
    public static void set(User user) {
        currentUser = user;
    }

    public static User get() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }
}
