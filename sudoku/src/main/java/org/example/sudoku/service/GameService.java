package org.example.sudoku.service;

import org.example.sudoku.domain.*;

import java.util.List;

public interface GameService {
    Puzzle newGame(Level level);
    GameState load(int saveId);
    GameState save(GameState state);
    List<Stat> stats();
}