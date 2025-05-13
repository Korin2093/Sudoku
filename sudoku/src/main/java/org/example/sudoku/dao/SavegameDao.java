package org.example.sudoku.dao;

import org.example.sudoku.domain.GameState;

public interface SavegameDao extends CrudRepository<GameState, Integer> {}