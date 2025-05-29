package org.example.sudoku.dao;

import javax.sql.DataSource;
import org.sqlite.SQLiteDataSource;

public class DataSourceUtil {

    private static final String DB_URL = "jdbc:sqlite:C:/Users/robok/IdeaProjects/Sudoku/sudoku.db";
    private static SQLiteDataSource dataSource;

    public static DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = new SQLiteDataSource();
            dataSource.setUrl(DB_URL);
        }
        return dataSource;
    }
}