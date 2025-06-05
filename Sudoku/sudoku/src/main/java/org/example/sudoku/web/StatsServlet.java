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

@WebServlet("/api/stats")
public class StatsServlet extends HttpServlet {
    
    private GameServiceImpl gameService;
    private ObjectMapper objectMapper;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.gameService = new GameServiceImpl();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            Map<String, Object> stats = gameService.getGameStatistics();
            
            String jsonResponse = objectMapper.writeValueAsString(stats);
            response.getWriter().write(jsonResponse);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get statistics: " + e.getMessage());
            response.getWriter().write(objectMapper.writeValueAsString(error));
        }
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
            String puzzleId = jsonNode.get("puzzleId").asText();
            int duration = jsonNode.get("duration").asInt();
            
            gameService.saveGameResult(puzzleId, duration);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            
            String jsonResponse = objectMapper.writeValueAsString(result);
            response.getWriter().write(jsonResponse);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to save game result: " + e.getMessage());
            response.getWriter().write(objectMapper.writeValueAsString(error));
        }
    }
}
