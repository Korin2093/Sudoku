package org.example.sudoku.dao;

import org.example.sudoku.domain.Level;
import org.example.sudoku.domain.Puzzle;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class PuzzleDaoImpl implements PuzzleDao {
    private final DataSource ds = DataSourceUtil.getDataSource();

    @Override
    public Puzzle save(Puzzle entity) {
        String sql = "INSERT INTO puzzle(seed, level) VALUES(?,?)";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entity.seed());
            ps.setString(2, entity.level().name());
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return new Puzzle(rs.getInt(1), entity.seed(), entity.level(), entity.grid());
            }
            throw new SQLException("ID not generated");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Puzzle> findById(Integer id) {
        String sql = "SELECT * FROM puzzle WHERE id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapToPuzzle(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Puzzle> findAll() {
        String sql = "SELECT * FROM puzzle";
        List<Puzzle> puzzles = new ArrayList<>();
        
        try (Connection conn = ds.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                puzzles.add(mapToPuzzle(rs));
            }
            return puzzles;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM puzzle WHERE id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Puzzle mapToPuzzle(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String seed = rs.getString("seed");
        String levelStr = rs.getString("level");
        Level level = Level.valueOf(levelStr);
        
        // Парсим seed в сетку
        int[][] grid = parseGrid(seed);
        
        return new Puzzle(id, seed, level, grid);
    }
    
    /**
     * Конвертирует строку seed в двумерный массив 9x9
     */
    private int[][] parseGrid(String seed) {
        int[][] grid = new int[9][9];
        if (seed != null && seed.length() == 81) {
            for (int i = 0; i < 81; i++) {
                char c = seed.charAt(i);
                int value = (c == '0' || c == '.') ? 0 : Character.getNumericValue(c);
                grid[i / 9][i % 9] = value;
            }
        }
        return grid;
    }
}
