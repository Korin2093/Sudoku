package org.example.sudoku;

import org.example.sudoku.util.FlywayMigrator;

public class Main {
    public static void main(String[] args) {
        FlywayMigrator.migrate();
        System.out.println("Migration done");
    }
}
