package org.example.sudoku.util;

import org.flywaydb.core.Flyway;
import org.example.sudoku.dao.DataSourceUtil;

public class FlywayMigrator {
    public static void migrate() {
        Flyway.configure()
                .dataSource(DataSourceUtil.getDataSource())
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }
}
