package edu.univ.erp.service;

import edu.univ.erp.data.MaintenanceDao;

import javax.sql.DataSource;
import java.sql.SQLException;

public class MaintenanceService {
    private final MaintenanceDao dao;

    //constructor
    public MaintenanceService(DataSource erpDS) {
        this.dao = new MaintenanceDao(erpDS);
    }

    //check if maintenance is on
    public boolean isMaintenanceOn() throws SQLException {
        return dao.isMaintenanceOn();
    }

    //if a function writes to data, check if maintenance is off, else throw error
    public void requireWriteAllowed() throws ServiceException {
        try {
            if (dao.isMaintenanceOn()) {
                throw new ServiceException("Maintenance mode is ON. Changes are temporarily disabled.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServiceException("Error checking maintenance mode.", e);
        }
    }

    //turn on maintenance
    public void turnOn() throws SQLException {
        dao.setMaintenance(true);
    }

    //turn off maintenance
    public void turnOff() throws SQLException {
        dao.setMaintenance(false);
    }

}