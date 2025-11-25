package edu.univ.erp.service;

import edu.univ.erp.data.UserDao;
import edu.univ.erp.domain.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final UserDao dao;

    public UserService(DataSource auth_ds, DataSource erp_ds) {
        this.dao = new UserDao(auth_ds, erp_ds);
    }

    public User loadUserProfile(User user) throws SQLException {
        return dao.findFullUserByUsername(user.getUsername());
    }

    public List<User> findAllUsers() throws SQLException {
        ArrayList<User> list = new ArrayList<>();

        return list;
    }

    public List<User> loadAllStudents() throws SQLException {
        return null;
    }

    public List<Instructor> loadAllInstructors() throws SQLException {
        ArrayList<Instructor> instructors = new ArrayList<>();
        return instructors;
    }
}
