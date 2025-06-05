package org.example.sudoku.dao;

import org.example.sudoku.domain.Stat;
import org.example.sudoku.domain.Puzzle;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class StatsDaoImpl implements StatsDao {
    private final DataSource ds = DataSourceUtil.getDataSource();
    private final PuzzleDao puzzleDao = new PuzzleDaoImpl();

    @Override
    public Stat save(Stat entity) {
        String sql = "INSERT INTO stats(puzzle_id, duration) VALUES(?,?)";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, entity.puzzle().id());
            ps.setInt(2, entity.durationSeconds());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return new Stat(rs.getInt(1), entity.puzzle(), entity.durationSeconds());
            }
            throw new SQLException("ID not generated");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Stat> findById(Integer id) {
        String sql = "SELECT * FROM stats WHERE id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Stat> findAll() {
        List<Stat> list = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM stats")) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM stats WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Stat map(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int puzzleId = rs.getInt("puzzle_id");
        int dur = rs.getInt("duration");
        
        // Загружаем puzzle из базы данных
        Puzzle puzzle = puzzleDao.findById(puzzleId).orElse(null);
        return new Stat(id, puzzle, dur);
    }
}