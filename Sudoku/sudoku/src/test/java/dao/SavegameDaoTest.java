package dao;

import org.example.sudoku.dao.*;
import org.example.sudoku.domain.*;
import org.example.sudoku.util.FlywayMigrator;
import org.junit.jupiter.api.*;
import java.util.Optional;

class SavegameDaoTest {
    private final SavegameDao dao = new SavegameDaoImpl();
    private final PuzzleDao puzzleDao = new PuzzleDaoImpl();
    
    @BeforeAll
    static void setupDatabase() {
        // Инициализируем базу данных для тестов
        FlywayMigrator.migrate();
    }

    @Test
    void saveAndLoad() {
        // Сначала создаем puzzle в базе данных
        Puzzle puzzle = puzzleDao.save(new Puzzle(0, "test-seed", Level.EASY, new int[9][9]));
        
        // Теперь создаем GameState с существующим puzzle
        GameState gameState = new GameState(0, puzzle, "board-snapshot", "preset-snapshot", 10);
        GameState saved = dao.save(gameState);

        Optional<GameState> loaded = dao.findById(saved.id());
        Assertions.assertTrue(loaded.isPresent());
        Assertions.assertEquals(saved.boardSnapshot(), loaded.get().boardSnapshot());
        Assertions.assertEquals(saved.presetSnapshot(), loaded.get().presetSnapshot());
        Assertions.assertEquals(saved.elapsedSeconds(), loaded.get().elapsedSeconds());
    }
}