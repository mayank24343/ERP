package edu.univ.erp.auth;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    private static final int WORK_FACTOR = 12;
    //constructor
    public static String hashPassword(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(WORK_FACTOR));
    }

    public static void main(String[] args) {
        String password = "password"; //to get password hash to put in db seed
        String hash = hashPassword(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
    }
}

