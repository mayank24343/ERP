package edu.univ.erp.auth;

import java.sql.Timestamp;
public class AuthResult {
    public final boolean ok;
    public final String userId;
    public final String role;
    public final boolean locked;
    public final Timestamp lockedUntil;
    public final String message;

    private AuthResult(boolean ok, String userId, String role, boolean locked, Timestamp lockedUntil, String message) {
        this.ok = ok; this.userId = userId; this.role = role; this.locked = locked; this.lockedUntil = lockedUntil; this.message = message;
    }

    public static AuthResult success(String userId, String role) { return new AuthResult(true, userId, role, false, null, "OK"); }
    public static AuthResult invalidCredentials() { return new AuthResult(false, null, null, false, null, "Invalid username or password."); }
    public static AuthResult locked(Timestamp until) { return new AuthResult(false, null, null, true, until, "Account locked until " + until.toString()); }
    public static AuthResult disabled() { return new AuthResult(false, null, null, false, null, "Account disabled. Contact admin."); }
    public static AuthResult error(String msg) { return new AuthResult(false, null, null, false, null, msg); }
}
