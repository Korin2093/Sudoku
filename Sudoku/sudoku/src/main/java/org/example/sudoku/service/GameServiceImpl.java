package org.example.sudoku.service;

import org.example.sudoku.dao.*;
import org.example.sudoku.domain.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

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
        int[][] grid = parseGrid(snapshot);                         // конвертируем в сетку
        Puzzle p = new Puzzle(0, snapshot, level, grid);           // seed = snapshot
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

    @Override
    public Puzzle getPuzzleById(int puzzleId) {
        return puzzleDao.findById(puzzleId).orElse(null);
    }

    @Override
    public List<GameState> getAllSaveGames() {
        return savegameDao.findAll();
    }

    @Override
    public void deleteSaveGame(int saveId) {
        savegameDao.deleteById(saveId);
    }
    
    @Override
    public Map<String, Object> getGameStatistics() {
        List<Stat> allStats = statsDao.findAll();
        Map<String, Object> statistics = new HashMap<>();
        
        // Общая статистика
        Map<String, Object> totalStats = new HashMap<>();
        totalStats.put("totalGames", allStats.size());
        
        if (!allStats.isEmpty()) {
            double avgTime = allStats.stream()
                .mapToInt(Stat::durationSeconds)
                .average()
                .orElse(0.0);
            int bestTime = allStats.stream()
                .mapToInt(Stat::durationSeconds)
                .min()
                .orElse(0);
            
            totalStats.put("avgTime", avgTime);
            totalStats.put("bestTime", bestTime);
        } else {
            totalStats.put("avgTime", 0.0);
            totalStats.put("bestTime", 0);
        }
        
        statistics.put("totalStats", totalStats);
        
        // Статистика по уровням
        Map<String, Object> levelStats = new HashMap<>();
        if (!allStats.isEmpty()) {
            Map<Level, List<Stat>> statsByLevel = allStats.stream()
                .filter(stat -> stat.puzzle() != null)
                .collect(Collectors.groupingBy(stat -> stat.puzzle().level()));
            
            for (Map.Entry<Level, List<Stat>> entry : statsByLevel.entrySet()) {
                List<Stat> levelStatsList = entry.getValue();
                Map<String, Object> levelData = new HashMap<>();
                
                levelData.put("gamesPlayed", levelStatsList.size());
                
                double levelAvgTime = levelStatsList.stream()
                    .mapToInt(Stat::durationSeconds)
                    .average()
                    .orElse(0.0);
                int levelBestTime = levelStatsList.stream()
                    .mapToInt(Stat::durationSeconds)
                    .min()
                    .orElse(0);
                
                levelData.put("avgTime", levelAvgTime);
                levelData.put("bestTime", levelBestTime);
                
                levelStats.put(entry.getKey().toString(), levelData);
            }
        }
        statistics.put("levelStats", levelStats);
        
        // История игр (последние 20 игр)
        List<Map<String, Object>> gameHistory = allStats.stream()
            .sorted((a, b) -> Integer.compare(b.id(), a.id())) // сортируем по ID в убывающем порядке
            .limit(20)
            .map(stat -> {
                Map<String, Object> gameData = new HashMap<>();
                gameData.put("level", stat.puzzle() != null ? stat.puzzle().level().toString() : "UNKNOWN");
                gameData.put("duration", stat.durationSeconds());
                gameData.put("formattedDuration", formatDuration(stat.durationSeconds()));
                return gameData;
            })
            .collect(Collectors.toList());
        
        statistics.put("gameHistory", gameHistory);
        
        return statistics;
    }
    
    private String formatDuration(int seconds) {
        if (seconds == 0) return "0:00";
        
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%d:%02d", minutes, secs);
        }
    }
    
    @Override
    public void saveGameResult(String puzzleId, int duration) {
        try {
            int puzzleIdInt = Integer.parseInt(puzzleId);
            Puzzle puzzle = puzzleDao.findById(puzzleIdInt).orElse(null);
            
            if (puzzle != null) {
                Stat stat = new Stat(0, puzzle, duration);
                statsDao.save(stat);
            } else {
                throw new RuntimeException("Puzzle with id " + puzzleId + " not found");
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid puzzle ID format: " + puzzleId);
        }
    }
    
    @Override
    public boolean validateMove(String seed, int row, int col, int value) {
        return canPlace(seed, row, col, value);
    }
    
    /**
     * Конвертирует строку snapshot в двумерный массив 9x9
     */
    private int[][] parseGrid(String snapshot) {
        int[][] grid = new int[9][9];
        if (snapshot != null && snapshot.length() == 81) {
            for (int i = 0; i < 81; i++) {
                char c = snapshot.charAt(i);
                int value = (c == '0' || c == '.') ? 0 : Character.getNumericValue(c);
                grid[i / 9][i % 9] = value;
            }
        }
        return grid;
    }
}
