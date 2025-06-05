package org.example.sudoku.dao;

import javax.sql.DataSource;
import org.sqlite.SQLiteDataSource;
import java.io.File;

public class DataSourceUtil {

    private static final String DB_URL = "jdbc:sqlite:sudoku.db";
    private static SQLiteDataSource dataSource;

    public static DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = new SQLiteDataSource();
            dataSource.setUrl(DB_URL);
            
            // Создаем файл базы данных если его нет
            try {
                File dbFile = new File("sudoku.db");
                if (!dbFile.exists()) {
                    System.out.println("Создание файла базы данных: " + dbFile.getAbsolutePath());
                    dbFile.createNewFile();
                }
            } catch (Exception e) {
                System.err.println("Ошибка при создании файла базы данных: " + e.getMessage());
            }
        }
        return dataSource;
    }
}