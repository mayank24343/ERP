package edu.univ.erp.service;

import edu.univ.erp.data.UserDao;
import edu.univ.erp.domain.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class UserService {
    private final UserDao dao;
    //constructor
    public UserService(DataSource auth_ds, DataSource erp_ds) {
        this.dao = new UserDao(auth_ds, erp_ds);
    }

    //load a user profile
    public User loadUserProfile(User user) throws SQLException {
        return dao.findFullUserByUsername(user.getUsername());
    }

    //to find all instructors
    public List<Instructor> loadAllInstructors() throws SQLException {
        return dao.findAllInstructors();
    }
}
