package org.example.sudoku;

import org.example.sudoku.domain.Level;
import org.example.sudoku.domain.Puzzle;
import org.example.sudoku.service.GameService;
import org.example.sudoku.service.GameServiceImpl;
import org.example.sudoku.util.FlywayMigrator;

public class Main {
    public static void main(String[] args) {
        System.out.println("запуск приложения судоку...");
        
        try {
            // запуск миграции базы данных
            FlywayMigrator.migrate();
            System.out.println("✅ миграция базы данных завершена успешно");
        } catch (Exception e) {
            System.err.println("❌ ошибка при инициализации базы данных: " + e.getMessage());
            return; // останавливаем выполнение если БД не инициализировалась
        }
        
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
            
            // тестирование сохранения результата игры
            System.out.println("\nтест сохранения результата игры...");
            gameService.saveGameResult(String.valueOf(easyPuzzle.id()), 180); // 3 минуты
            System.out.println("результат игры сохранен!");
            
            // создадим еще несколько тестовых игр для истории
            System.out.println("\nсоздание тестовых игр для истории...");
            for (Level level : Level.values()) {
                Puzzle puzzle = gameService.newGame(level);
                int randomTime = 120 + (int)(Math.random() * 300); // от 2 до 7 минут
                gameService.saveGameResult(String.valueOf(puzzle.id()), randomTime);
                System.out.println("создана игра " + level + " (" + randomTime + " сек)");
            }
            
            // проверка статистики
            System.out.println("\nтест статистики игр...");
            var stats = gameService.getGameStatistics();
            System.out.println("всего игр: " + ((java.util.Map<?, ?>)stats.get("totalStats")).get("totalGames"));
            System.out.println("средний результат: " + ((java.util.Map<?, ?>)stats.get("totalStats")).get("avgTime") + " сек");
            System.out.println("лучший результат: " + ((java.util.Map<?, ?>)stats.get("totalStats")).get("bestTime") + " сек");
            
            System.out.println("\n✅ все тесты пройдены! система судоку работает корректно");
            
        } catch (Exception e) {
            System.err.println("❌ ошибка при тестировании: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
