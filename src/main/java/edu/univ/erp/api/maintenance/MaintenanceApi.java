package edu.univ.erp.api.maintenance;

import edu.univ.erp.api.common.ApiResult;
import edu.univ.erp.service.MaintenanceService;
import edu.univ.erp.util.DataSourceProvider;

public class MaintenanceApi {
    private final MaintenanceService maintenanceService;
    public MaintenanceApi() {
        this.maintenanceService = new MaintenanceService(DataSourceProvider.getERPDataSource());
    }

    //return if maintenance is on or off
    public ApiResult<Boolean> isMaintenanceOn(){
        try {
            return ApiResult.ok(maintenanceService.isMaintenanceOn());
        } catch(Exception e){
            return ApiResult.error(e.getMessage());
        }
    }
}
