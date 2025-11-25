package edu.univ.erp.api.catalog;

import javax.sql.DataSource;

public class CatalogApi {
    private DataSource ds;
    public CatalogApi(DataSource ds) {
        this.ds = ds;
    }


}
