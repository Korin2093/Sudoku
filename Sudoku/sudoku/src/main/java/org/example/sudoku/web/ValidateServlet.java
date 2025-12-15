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
            String seed = requireBoardSeed(jsonNode);
            int row = requireCoordinate(jsonNode, "row");
            int col = requireCoordinate(jsonNode, "col");
            int value = requireDigit(jsonNode);
            
            boolean isValid = gameService.validateMove(seed, row, col, value);
            
            Map<String, Object> result = new HashMap<>();
            result.put("valid", isValid);
            
            String jsonResponse = objectMapper.writeValueAsString(result);
            response.getWriter().write(jsonResponse);
            
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            response.getWriter().write(objectMapper.writeValueAsString(error));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Validation failed: " + e.getMessage());
            response.getWriter().write(objectMapper.writeValueAsString(error));
        }
    }

    private String requireBoardSeed(JsonNode jsonNode) {
        if (jsonNode.hasNonNull("seed")) {
            String seed = jsonNode.get("seed").asText();
            if (seed != null && seed.matches("[0-9]{81}")) {
                return seed;
            }
        }
        throw new IllegalArgumentException("Field 'seed' must contain exactly 81 digits");
    }

    private int requireCoordinate(JsonNode jsonNode, String field) {
        if (jsonNode.hasNonNull(field)) {
            int value = jsonNode.get(field).asInt();
            if (value >= 0 && value < 9) {
                return value;
            }
        }
        throw new IllegalArgumentException("Field '" + field + "' must be between 0 and 8");
    }

    private int requireDigit(JsonNode jsonNode) {
        if (jsonNode.hasNonNull("value")) {
            int digit = jsonNode.get("value").asInt();
            if (digit >= 1 && digit <= 9) {
                return digit;
            }
        }
        throw new IllegalArgumentException("Field 'value' must be between 1 and 9");
    }
}
