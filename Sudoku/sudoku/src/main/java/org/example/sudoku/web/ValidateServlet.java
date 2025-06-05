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

@WebServlet("/api/validate")
public class ValidateServlet extends HttpServlet {
    
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
            String seed = jsonNode.get("seed").asText();
            int row = jsonNode.get("row").asInt();
            int col = jsonNode.get("col").asInt();
            int value = jsonNode.get("value").asInt();
            
            boolean isValid = gameService.validateMove(seed, row, col, value);
            
            Map<String, Object> result = new HashMap<>();
            result.put("valid", isValid);
            
            String jsonResponse = objectMapper.writeValueAsString(result);
            response.getWriter().write(jsonResponse);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Validation failed: " + e.getMessage());
            response.getWriter().write(objectMapper.writeValueAsString(error));
        }
    }
}
