package org.example.sudoku.service;

import org.example.sudoku.domain.*;

import java.util.List;
import java.util.Map;

/** контракт сервисного слоя игры Sudoku */
public interface GameService {
    Puzzle newGame(Level level);
    GameState load(int saveId);
    GameState save(GameState state);
    List<Stat> stats();

    /** решить доску полностью */
    String solve(String snapshot);

    /** проверить можно ли поставить digit (1‒9) в клетку (row, col) */
    boolean canPlace(String snapshot, int row, int col, int digit);
    
    /** получить статистику игр */
    Map<String, Object> getGameStatistics();
    
    /** сохранить результат игры */
    void saveGameResult(String puzzleId, int duration);
    
    /** проверить корректность хода */
    boolean validateMove(String seed, int row, int col, int value);

    /** получить puzzle по ID */
    Puzzle getPuzzleById(int puzzleId);

    /** получить все сохранения игр */
    List<GameState> getAllSaveGames();

    /** удалить сохранение игры */
    void deleteSaveGame(int saveId);
}
