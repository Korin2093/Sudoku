package org.example.sudoku.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.sudoku.domain.GameState;
import org.example.sudoku.domain.Puzzle;
import org.example.sudoku.service.GameServiceImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/api/savegame/*")
public class SaveGameServlet extends HttpServlet {

    private GameServiceImpl gameService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        this.gameService = new GameServiceImpl();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String jsonBody = request.getReader().lines()
                    .collect(Collectors.joining(System.lineSeparator()));

            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = objectMapper.readValue(jsonBody, Map.class);

            int puzzleId = (Integer) requestData.get("puzzleId");
            String boardSnapshot = (String) requestData.get("boardSnapshot");
            String presetSnapshot = (String) requestData.get("presetSnapshot");
            int elapsedSeconds = (Integer) requestData.get("elapsedSeconds");

            // Получаем puzzle по ID
            Puzzle puzzle = gameService.getPuzzleById(puzzleId);
            if (puzzle == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                Map<String, String> error = new HashMap<>();
                error.put("error", "Puzzle not found");
                response.getWriter().write(objectMapper.writeValueAsString(error));
                return;
            }

            GameState gameState = new GameState(0, puzzle, boardSnapshot, presetSnapshot, elapsedSeconds);
            GameState savedState = gameService.save(gameState);

            Map<String, Object> result = new HashMap<>();
            result.put("saveId", savedState.id());
            result.put("message", "Game saved successfully");

            String jsonResponse = objectMapper.writeValueAsString(result);
            response.getWriter().write(jsonResponse);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to save game: " + e.getMessage());
            response.getWriter().write(objectMapper.writeValueAsString(error));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Получить список всех сохранений
                List<GameState> saveGames = gameService.getAllSaveGames();

                List<Map<String, Object>> result = saveGames.stream()
                        .map(this::mapGameStateToResponse)
                        .collect(Collectors.toList());

                String jsonResponse = objectMapper.writeValueAsString(result);
                response.getWriter().write(jsonResponse);

            } else {
                // Загрузить конкретное сохранение
                String saveIdStr = pathInfo.substring(1); // убираем '/'
                int saveId = Integer.parseInt(saveIdStr);

                GameState gameState = gameService.load(saveId);
                if (gameState != null) {
                    Map<String, Object> result = mapGameStateToResponse(gameState);
                    String jsonResponse = objectMapper.writeValueAsString(result);
                    response.getWriter().write(jsonResponse);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Save game not found");
                    response.getWriter().write(objectMapper.writeValueAsString(error));
                }
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid save ID");
            response.getWriter().write(objectMapper.writeValueAsString(error));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to load game: " + e.getMessage());
            response.getWriter().write(objectMapper.writeValueAsString(error));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo != null && !pathInfo.equals("/")) {
                String saveIdStr = pathInfo.substring(1);
                int saveId = Integer.parseInt(saveIdStr);

                gameService.deleteSaveGame(saveId);

                Map<String, String> result = new HashMap<>();
                result.put("message", "Save game deleted successfully");
                String jsonResponse = objectMapper.writeValueAsString(result);
                response.getWriter().write(jsonResponse);

            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, String> error = new HashMap<>();
                error.put("error", "Save ID is required");
                response.getWriter().write(objectMapper.writeValueAsString(error));
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid save ID");
            response.getWriter().write(objectMapper.writeValueAsString(error));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete save game: " + e.getMessage());
            response.getWriter().write(objectMapper.writeValueAsString(error));
        }
    }

    private Map<String, Object> mapGameStateToResponse(GameState gameState) {
        Map<String, Object> result = new HashMap<>();
        result.put("saveId", gameState.id());
        result.put("puzzleId", gameState.puzzle().id());
        result.put("boardSnapshot", gameState.boardSnapshot());
        result.put("presetSnapshot", gameState.presetSnapshot());
        result.put("elapsedSeconds", gameState.elapsedSeconds());
        result.put("level", gameState.puzzle().level().toString());
        result.put("formattedTime", formatDuration(gameState.elapsedSeconds()));

        return result;
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
}