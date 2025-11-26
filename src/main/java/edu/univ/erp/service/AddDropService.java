package edu.univ.erp.service;

import edu.univ.erp.api.maintenance.MaintenanceApi;
import edu.univ.erp.data.AddDropDao;
import edu.univ.erp.ui.UiContext;
import edu.univ.erp.util.DataSourceProvider;

import java.time.LocalDate;

public class AddDropService {
    private final AddDropDao dao;
    private final MaintenanceService maintenance;

    //constructor
    public AddDropService() {
        this.dao = new AddDropDao(DataSourceProvider.getERPDataSource());
        this.maintenance = new MaintenanceService(DataSourceProvider.getERPDataSource());
    }

    //change deadline
    public void updateDeadline(LocalDate d) throws Exception {
        UiContext.get().access().requireAdmin();
        maintenance.requireWriteAllowed();
        dao.setDeadline(d);
    }

    //get deadline
    public LocalDate getDeadline() throws Exception {
        return dao.getDeadline();
    }
}
