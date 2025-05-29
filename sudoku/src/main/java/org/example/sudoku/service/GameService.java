package org.example.sudoku.service;

import org.example.sudoku.domain.*;

import java.util.List;

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
}
