package edu.univ.erp.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceProvider {
    private static HikariDataSource ds;

    public static DataSource getDataSource() {
        if (ds == null) {
            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl("jdbc:mysql://localhost:3306/auth_db?useSSL=false&serverTimezone=UTC");
            cfg.setUsername("root");
            cfg.setPassword("@F83B821Ssmall");
            cfg.setMaximumPoolSize(10);
            cfg.setMinimumIdle(2);
            cfg.setConnectionTimeout(20000);
            cfg.setPoolName("AuthHikariPool");
            ds = new HikariDataSource(cfg);
        }
        return ds;
    }
}
