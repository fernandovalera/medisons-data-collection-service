package com.medisons.dbm;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariConnectionManager extends ConnectionManager {

    private HikariDataSource ds;

    public HikariConnectionManager(String url) {
        super(url);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        // https://github.com/brettwooldridge/HikariCP/issues/1268
        config.addDataSourceProperty("sslMode", "DISABLED");
        config.addDataSourceProperty("allowPublicKeyRetrieval", "false");

        ds = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
