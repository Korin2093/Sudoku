package org.example.sudoku.util;

import org.flywaydb.core.Flyway;
import org.example.sudoku.dao.DataSourceUtil;
import java.util.Arrays;

public class FlywayMigrator {
    public static void migrate() {
        try {
            System.out.println("Запуск миграции базы данных...");
            
            Flyway flyway = Flyway.configure()
                    .dataSource(DataSourceUtil.getDataSource())
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true) // Позволяет применить миграции на существующую БД
                    .validateOnMigrate(false) // Отключаем валидацию для избежания проблем
                    .load();
                    
            int migrationsApplied = flyway.migrate().migrationsExecuted;
            System.out.println("Применено миграций: " + migrationsApplied);
            
            // Проверим состояние БД
            Arrays.stream(flyway.info().all()).forEach(info -> 
                System.out.println("Миграция: " + info.getVersion() + " - " + info.getState())
            );
            
        } catch (Exception e) {
            System.err.println("Ошибка при миграции базы данных: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Не удалось выполнить миграцию базы данных", e);
        }
    }
}
