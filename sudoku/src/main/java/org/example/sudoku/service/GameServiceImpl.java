package org.example.sudoku.service;

import org.example.sudoku.dao.*;
import org.example.sudoku.domain.*;
import java.util.List;

public class GameServiceImpl implements GameService {
    private final PuzzleDao puzzleDao;
    private final SavegameDao savegameDao;
    private final StatsDao statsDao;

    public GameServiceImpl(PuzzleDao puzzleDao,
                           SavegameDao savegameDao,
                           StatsDao statsDao) {
        this.puzzleDao = puzzleDao;
        this.savegameDao = savegameDao;
        this.statsDao = statsDao;
    }

    // Конструктор по умолчанию с реализациями DAO
    public GameServiceImpl() {
        this.puzzleDao = new PuzzleDaoImpl();
        this.savegameDao = new SavegameDaoImpl();
        this.statsDao = new StatsDaoImpl();
    }

    @Override
    public Puzzle newGame(Level level) {
        String snapshot = PuzzleGenerator.generate(level);          // ← генерация
        Puzzle p = new Puzzle(0, snapshot, level);                  // seed = snapshot
        return puzzleDao.save(p);
    }

    @Override
    public GameState load(int saveId) {
        return savegameDao.findById(saveId).orElse(null);
    }

    @Override
    public GameState save(GameState state) {
        return savegameDao.save(state);
    }

    @Override
    public List<Stat> stats() {
        return statsDao.findAll();
    }

    @Override
    public String solve(String snapshot) {
        return new SolverServiceImpl().solve(snapshot);
    }

    @Override
    public boolean canPlace(String snapshot, int row, int col, int digit) {
        return BoardValidator.canPlace(snapshot, row, col, digit);
    }
}
