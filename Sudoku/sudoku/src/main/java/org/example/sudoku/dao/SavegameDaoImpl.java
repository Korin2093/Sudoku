package org.example.sudoku.dao;

import org.example.sudoku.domain.GameState;
import org.example.sudoku.domain.Puzzle;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class SavegameDaoImpl implements SavegameDao {
    private final DataSource ds = DataSourceUtil.getDataSource();
    private final PuzzleDao puzzleDao = new PuzzleDaoImpl();

    @Override
    public GameState save(GameState entity) {
        // проверяем, есть ли колонка preset_snapshot
        if (hasPresetSnapshotColumn()) {
            String sql = "INSERT INTO savegame(puzzle_id, board, preset_snapshot, seconds) VALUES(?,?,?,?)";
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, entity.puzzle().id());
                ps.setString(2, entity.boardSnapshot());
                ps.setString(3, entity.presetSnapshot());
                ps.setInt(4, entity.elapsedSeconds());
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return new GameState(rs.getInt(1), entity.puzzle(), entity.boardSnapshot(), entity.presetSnapshot(), entity.elapsedSeconds());
                }
                throw new SQLException("ID not generated");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            // откатываемся на старую схему без preset_snapshot
            String sql = "INSERT INTO savegame(puzzle_id, board, seconds) VALUES(?,?,?)";
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, entity.puzzle().id());
                ps.setString(2, entity.boardSnapshot());
                ps.setInt(3, entity.elapsedSeconds());
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return new GameState(rs.getInt(1), entity.puzzle(), entity.boardSnapshot(), null, entity.elapsedSeconds());
                }
                throw new SQLException("ID not generated");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Optional<GameState> findById(Integer id) {
        String sql = "SELECT * FROM savegame WHERE id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(map(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<GameState> findAll() {
        String sql = "SELECT * FROM savegame";
        List<GameState> list = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM savegame WHERE id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private GameState map(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int puzzleId = rs.getInt("puzzle_id");
        String board = rs.getString("board");
        
        // аккуратно читаем preset_snapshot, в старой схеме его может не быть
        String presetSnapshot = null;
        try {
            presetSnapshot = rs.getString("preset_snapshot");
        } catch (SQLException e) {
            // колонки нет, значит ставим null
            presetSnapshot = null;
        }
        
        int sec = rs.getInt("seconds");
        
        // Загружаем puzzle из базы данных
        Puzzle puzzle = puzzleDao.findById(puzzleId).orElse(null);
        return new GameState(id, puzzle, board, presetSnapshot, sec);
    }
    
    private boolean hasPresetSnapshotColumn() {
        String sql = "PRAGMA table_info(savegame)";
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String columnName = rs.getString("name");
                if ("preset_snapshot".equals(columnName)) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error checking column existence: " + e.getMessage());
            return false;
        }
    }
}