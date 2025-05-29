package org.example.sudoku.dao;

import org.example.sudoku.domain.*;
import org.junit.jupiter.api.*;
import java.util.Optional;

class SavegameDaoTest {
    private final SavegameDao dao = new SavegameDaoImpl();

    @Test
    void saveAndLoad() {
        Puzzle dummyPuzzle = new Puzzle(1, "seed", Level.EASY);
        GameState saved = dao.save(new GameState(0, dummyPuzzle, "board", 10));

        Optional<GameState> loaded = dao.findById(saved.id());
        Assertions.assertTrue(loaded.isPresent());
        Assertions.assertEquals(saved.boardSnapshot(), loaded.get().boardSnapshot());
    }
}