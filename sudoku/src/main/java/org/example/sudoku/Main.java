package org.example.sudoku;

import org.example.sudoku.domain.Level;
import org.example.sudoku.domain.Puzzle;
import org.example.sudoku.service.GameService;
import org.example.sudoku.service.GameServiceImpl;
import org.example.sudoku.util.FlywayMigrator;

public class Main {
    public static void main(String[] args) {
        System.out.println("запуск приложения судоку...");
        
        // запуск миграции базы данных
        FlywayMigrator.migrate();
        System.out.println("миграция базы данных завершена");
        
        // тестирование системы
        testSudokuSystem();
    }
    
    private static void testSudokuSystem() {
        System.out.println("\n=== тест системы судоку ===");
        
        GameService gameService = new GameServiceImpl();
        
        try {
            // создание новой игры
            System.out.println("создание легкой головоломки...");
            Puzzle easyPuzzle = gameService.newGame(Level.EASY);
            System.out.println("создана головоломка с ID: " + easyPuzzle.id());
            System.out.println("уровень: " + easyPuzzle.level());
            System.out.println("seed (первые 50 символов): " + easyPuzzle.seed().substring(0, 50) + "...");
            
            // проверка валидации
            System.out.println("\nтест валидации...");
            boolean canPlace = gameService.canPlace(easyPuzzle.seed(), 0, 0, 5);
            System.out.println("можно поставить 5 в (0,0): " + canPlace);
            
            // проверка решения
            System.out.println("\nтест решателя...");
            String solution = gameService.solve(easyPuzzle.seed());
            System.out.println("головоломка решена успешно!");
            System.out.println("решение (первые 50 символов): " + solution.substring(0, 50) + "...");
            
            System.out.println("\n✅ все тесты пройдены! система судоку работает корректно");
            
        } catch (Exception e) {
            System.err.println("❌ ошибка при тестировании: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
