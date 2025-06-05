package org.example.sudoku.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.sudoku.domain.Level;
import org.example.sudoku.domain.Puzzle;
import org.example.sudoku.service.GameServiceImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/new")
public class NewGameServlet extends HttpServlet {
    
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
            String levelParam = request.getParameter("level");
            if (levelParam == null || levelParam.trim().isEmpty()) {
                levelParam = "EASY";
            }
            
            Level level = Level.valueOf(levelParam.toUpperCase());
            Puzzle puzzle = gameService.newGame(level);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", puzzle.id());
            result.put("seed", puzzle.seed());
            result.put("level", puzzle.level().toString());
            result.put("grid", puzzle.grid());
            
            String jsonResponse = objectMapper.writeValueAsString(result);
            response.getWriter().write(jsonResponse);
            
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid level parameter: " + request.getParameter("level"));
            response.getWriter().write(objectMapper.writeValueAsString(error));
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            response.getWriter().write(objectMapper.writeValueAsString(error));
        }
    }
}
