package edu.univ.erp.service;

import edu.univ.erp.access.CurrentSession;
import edu.univ.erp.data.AuthDao;
import edu.univ.erp.domain.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class AuthService {

    private final AuthDao authDao;
    private final int MAX_ATTEMPTS = 3;
    private final int LOCK_MINUTES = 3;

    //constructor
    public AuthService(DataSource authDS) {
        this.authDao = new AuthDao(authDS);
    }

    //login service
    public User login(String username, String password) throws ServiceException, SQLException {
        Optional<User> maybe = authDao.findByUsername(username);
        //optional to handle null objects

        if (maybe.isEmpty())
            throw new ServiceException("Incorrect username or password.");

        User user = maybe.get();

        //if account is locked right now
        if ("locked".equalsIgnoreCase(user.getStatus())) {

            //if current time is not after locked until time, throw error
            if (user.getLockedUntil() != null &&
                    user.getLockedUntil().after(Timestamp.from(Instant.now()))) {

                long minutesLeft = ChronoUnit.MINUTES.between(
                        Instant.now(), user.getLockedUntil().toInstant());

                throw new ServiceException("Account locked. Try again in " + minutesLeft + " minutes.");
            }

            //otherwise reset the lock
            authDao.unlockUser(user.getUserId());
            user = authDao.findByUsername(username).get();
        }

        //check password correctness
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {

            authDao.incrementFailedAttempts(user.getUserId());

            //if too many failed attempts, lock the account for some time
            if (user.getFailedAttempts() + 1 >= MAX_ATTEMPTS) {
                Timestamp until = Timestamp.from(Instant.now().plus(LOCK_MINUTES, ChronoUnit.MINUTES));
                authDao.lockUser(user.getUserId(), until);
                throw new ServiceException("Too many failed attempts. Account locked for " + LOCK_MINUTES + " minutes.");
            }

            throw new ServiceException("Incorrect username or password.");
        }

        //login finally
        authDao.resetFailedAttempts(user.getUserId());
        authDao.updateLastLogin(user.getUserId());

        //set current session for user
        CurrentSession.set(user);

        return user;
    }

    //logout
    public void logout() {
        try {
            CurrentSession.clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
