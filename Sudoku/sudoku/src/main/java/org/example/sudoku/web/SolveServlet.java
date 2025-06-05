package org.example.sudoku.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.sudoku.service.GameServiceImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/solve")
public class SolveServlet extends HttpServlet {
    
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
            // Читаем JSON из тела запроса
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            JsonNode jsonNode = objectMapper.readTree(sb.toString());
            String seed;
            
            // Проверяем, есть ли поле seed или puzzleId
            if (jsonNode.has("seed") && jsonNode.get("seed") != null) {
                seed = jsonNode.get("seed").asText();
            } else if (jsonNode.has("puzzleId") && jsonNode.get("puzzleId") != null) {
                // Если передан puzzleId, получаем seed из базы данных
                int puzzleId = jsonNode.get("puzzleId").asInt();
                var puzzle = gameService.getPuzzleById(puzzleId);
                if (puzzle == null) {
                    throw new IllegalArgumentException("Puzzle not found with ID: " + puzzleId);
                }
                seed = puzzle.seed();
            } else {
                throw new IllegalArgumentException("Either 'seed' or 'puzzleId' must be provided");
            }
            
            String solution = gameService.solve(seed);
            
            Map<String, Object> result = new HashMap<>();
            result.put("solution", solution);
            
            String jsonResponse = objectMapper.writeValueAsString(result);
            response.getWriter().write(jsonResponse);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unable to solve puzzle: " + e.getMessage());
            response.getWriter().write(objectMapper.writeValueAsString(error));
        }
    }
}
