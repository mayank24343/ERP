package edu.univ.erp.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceProvider {
    private static HikariDataSource auth_ds;
    private static HikariDataSource erp_ds;

    public static DataSource getAuthDataSource() {
        if (auth_ds == null) {
            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl("jdbc:mysql://localhost:3306/auth_db?useSSL=false&serverTimezone=UTC");
            cfg.setUsername("root");
            cfg.setPassword("@F83B821Ssmall");
            cfg.setMaximumPoolSize(10);
            cfg.setMinimumIdle(2);
            cfg.setConnectionTimeout(20000);
            cfg.setPoolName("AuthHikariPool");
            auth_ds = new HikariDataSource(cfg);
        }
        return auth_ds;
    }

    public static DataSource getERPDataSource() {
        if (erp_ds == null) {
            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl("jdbc:mysql://localhost:3306/erp_db?useSSL=false&serverTimezone=UTC");
            cfg.setUsername("root");
            cfg.setPassword("@F83B821Ssmall");
            cfg.setMaximumPoolSize(10);
            cfg.setMinimumIdle(2);
            cfg.setConnectionTimeout(20000);
            cfg.setPoolName("AuthHikariPool");
            erp_ds = new HikariDataSource(cfg);
        }
        return erp_ds;
    }
}
