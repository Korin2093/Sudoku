<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="messages"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Судоку</title>
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>
    <div class="container">
        <h1>Судоку</h1>
        
        <div class="controls">
            <label for="levelSelect">Уровень сложности:</label>
            <select id="levelSelect">
                <option value="EASY">Легкий</option>
                <option value="MEDIUM">Средний</option>
                <option value="HARD">Сложный</option>
            </select>
            <button id="newGameBtn">Новая игра</button>
            <button id="saveGameBtn">Сохранить игру</button>
            <button id="loadGameBtn">Загрузить игру</button>
            <button id="statsBtn">История игр</button>
        </div>

        <div class="game-area">
            <table id="sudokuGrid">
                <tbody>
                    <% for (int row = 0; row < 9; row++) { %>
                        <tr>
                            <% for (int col = 0; col < 9; col++) { %>
                                <td>
                                    <input type="text" 
                                           class="cell" 
                                           data-row="<%= row %>" 
                                           data-col="<%= col %>"
                                           maxlength="1">
                                </td>
                            <% } %>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>

        <div class="action-buttons">
            <button id="validateBtn">Проверить</button>
            <button id="solveBtn">Решить</button>
        </div>

        <div id="message" class="message"></div>
    </div>

    <!-- Модальное окно для истории игр -->
    <div id="statsModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>История игр и статистика</h2>
                <span class="close">&times;</span>
            </div>
            <div class="modal-body">
                <div class="stats-section">
                    <h3>Общая статистика</h3>
                    <div id="totalStats" class="stats-grid">
                        <!-- Заполняется JavaScript -->
                    </div>
                </div>
                
                <div class="stats-section">
                    <h3>Статистика по уровням</h3>
                    <div id="levelStats" class="level-stats">
                        <!-- Заполняется JavaScript -->
                    </div>
                </div>
                
                <div class="stats-section">
                    <h3>Недавние игры</h3>
                    <div id="gameHistory" class="game-history">
                        <!-- Заполняется JavaScript -->
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="js/app.js"></script>
    <!-- Модальное окно для сохранений -->
    <div id="savedGamesModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Сохраненные игры</h2>
                <span class="close">&times;</span>
            </div>
            <div class="modal-body">
                <div id="savedGamesList" class="saved-games-list">
                </div>
            </div>
        </div>
    </div>
</body>
</html>
