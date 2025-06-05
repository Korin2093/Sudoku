class SudokuGame {
    constructor() {
        this.currentGame = null;
        this.grid = null;
        this.isGameSolved = false;
        this.gameStartTime = null;
        this.debugMode = true;

        console.log('[SUDOKU] Initializing SudokuGame...');

        try {
            this.initializeElements();
            this.attachEventListeners();
            console.log('[SUDOKU] ✅ SudokuGame initialized successfully');
        } catch (error) {
            console.error('[SUDOKU] ❌ Error initializing SudokuGame:', error);
        }
    }

    initializeElements() {
        console.log('[SUDOKU] Finding DOM elements...');

        this.saveGameBtn = document.getElementById('saveGameBtn');
        this.loadGameBtn = document.getElementById('loadGameBtn');
        this.levelSelect = document.getElementById('levelSelect');
        this.newGameBtn = document.getElementById('newGameBtn');
        this.validateBtn = document.getElementById('validateBtn');
        this.solveBtn = document.getElementById('solveBtn');
        this.messageDiv = document.getElementById('message');
        this.sudokuGrid = document.getElementById('sudokuGrid');
        this.statsBtn = document.getElementById('statsBtn');
        this.cells = document.querySelectorAll('.cell');

        // Проверяем, что все элементы найдены
        const elements = {
            levelSelect: this.levelSelect,
            newGameBtn: this.newGameBtn,
            validateBtn: this.validateBtn,
            solveBtn: this.solveBtn,
            messageDiv: this.messageDiv,
            sudokuGrid: this.sudokuGrid,
            statsBtn: this.statsBtn,
            saveGameBtn: this.saveGameBtn,
            loadGameBtn: this.loadGameBtn
        };

        for (const [name, element] of Object.entries(elements)) {
            if (!element) {
                throw new Error(`Element not found: ${name}`);
            }
            console.log(`[SUDOKU] ✅ Found ${name}`);
        }

        console.log(`[SUDOKU] ✅ Found ${this.cells.length} cells`);
    }

    attachEventListeners() {
        console.log('[SUDOKU] Attaching event listeners...');

        // Новая игра - максимально просто, как в диагностике
        this.newGameBtn.addEventListener('click', async () => {
            console.log('[SUDOKU] New Game button clicked');
            await this.startNewGame();
        });

        // Валидация
        this.validateBtn.addEventListener('click', () => {
            console.log('[SUDOKU] Validate button clicked');
            this.validateCurrentState();
        });

        // Решение
        this.solveBtn.addEventListener('click', async () => {
            console.log('[SUDOKU] Solve button clicked');
            await this.solvePuzzle();
        });

        // Статистика
        this.statsBtn.addEventListener('click', async () => {
            console.log('[SUDOKU] Stats button clicked');
            await this.showGameHistory();
        });

        // Сохранение игры
        this.saveGameBtn.addEventListener('click', async () => {
            console.log('[SUDOKU] Save Game button clicked');
            await this.saveCurrentGame();
        });

        // Загрузка игры
        this.loadGameBtn.addEventListener('click', async () => {
            console.log('[SUDOKU] Load Game button clicked');
            await this.showSavedGames();
        });

        // Ввод в ячейки
        this.cells.forEach((cell, index) => {
            cell.addEventListener('input', (e) => {
                this.handleCellInput(e, index);
            });
        });


        this.setupModal();
        console.log('[SUDOKU] ✅ Event listeners attached');
    }

    async startNewGame() {
        const level = this.levelSelect.value;
        console.log('[SUDOKU] Starting new game with level:', level);

        this.showMessage('Создание новой игры...', 'info');

        try {
            // Точно такой же запрос, как в диагностике
            const response = await fetch(`/api/new?level=${level}`);
            const data = await response.json();

            console.log('[SUDOKU] New game response:', { status: response.status, data });

            if (response.ok) {
                // сохраняем все данные игры вместе с seed
                this.currentGame = {
                    id: data.id,
                    seed: data.seed,
                    level: data.level,
                    grid: data.grid
                };
                this.isGameSolved = false;
                this.gameStartTime = Date.now();

                console.log('[SUDOKU] ✅ Game created:', {
                    id: data.id,
                    level: data.level,
                    seedLength: data.seed ? data.seed.length : 'undefined'
                });

                this.renderGrid(data.seed);
                this.showMessage(`Новая игра начата! (ID: ${data.id})`, 'success');

            } else {
                throw new Error(data.error || 'Не удалось создать игру');
            }
        } catch (error) {
            console.error('[SUDOKU] ❌ Error creating new game:', error);
            this.showMessage(`Ошибка: ${error.message}`, 'error');
        }
    }

    getPresetState() {
        let state = '';
        this.cells.forEach((cell) => {
            // '1' если ячейка preset, '0' если пользовательская
            state += cell.classList.contains('preset') ? '1' : '0';
        });
        return state;
    }

    async saveCurrentGame() {
        console.log('[SUDOKU] Saving current game...');

        if (!this.currentGame || this.isGameSolved) {
            this.showMessage('Нет активной игры для сохранения', 'warning');
            return;
        }

        const currentState = this.getCurrentGameState();
        const presetState = this.getPresetState();
        const elapsedTime = this.gameStartTime ? Math.floor((Date.now() - this.gameStartTime) / 1000) : 0;

        const saveData = {
            puzzleId: this.currentGame.id,
            boardSnapshot: currentState,
            presetSnapshot: presetState,
            elapsedSeconds: elapsedTime
        };

        console.log('[SUDOKU] Save data:', saveData);

        try {
            const response = await fetch('/api/savegame', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(saveData)
            });

            const data = await response.json();
            console.log('[SUDOKU] Save response:', { status: response.status, data });

            if (response.ok) {
                this.showMessage(`Игра сохранена! ID: ${data.saveId}`, 'success');
            } else {
                throw new Error(data.error || 'Не удалось сохранить игру');
            }
        } catch (error) {
            console.error('[SUDOKU] ❌ Error saving game:', error);
            this.showMessage(`Ошибка сохранения: ${error.message}`, 'error');
        }
    }

    async loadSavedGame(saveId) {
        console.log('[SUDOKU] Loading saved game:', saveId);

        try {
            const response = await fetch(`/api/savegame/${saveId}`);
            const data = await response.json();

            console.log('[SUDOKU] Load response:', { status: response.status, data });

            if (response.ok) {
                // сначала берем puzzle, чтобы получить seed
                const puzzleResponse = await fetch(`/api/puzzle/${data.puzzleId}`);
                let seed = null;

                if (puzzleResponse.ok) {
                    const puzzleData = await puzzleResponse.json();
                    seed = puzzleData.seed;
                }

                // восстанавливаем состояние игры
                this.currentGame = {
                    id: data.puzzleId,
                    level: data.level,
                    seed: seed || null // берем найденный seed или id головоломки
                };

                this.isGameSolved = false;
                this.gameStartTime = Date.now() - (data.elapsedSeconds * 1000);

                // показываем загруженную игру с учётом preset ячеек
                this.renderLoadedGame(data.boardSnapshot, data.presetSnapshot);
                this.showMessage(`Игра загружена! Время: ${data.formattedTime}`, 'success');

            } else {
                throw new Error(data.error || 'Не удалось загрузить игру');
            }
        } catch (error) {
            console.error('[SUDOKU] ❌ Error loading game:', error);
            this.showMessage(`Ошибка загрузки: ${error.message}`, 'error');
        }
    }

    renderLoadedGame(boardSnapshot, presetSnapshot) {
        console.log('[SUDOKU] Rendering loaded game:', {
            boardSnapshot: boardSnapshot?.substring(0, 20) + '...',
            presetSnapshot: presetSnapshot?.substring(0, 20) + '...'
        });

        if (!boardSnapshot || boardSnapshot.length !== 81) {
            console.error('[SUDOKU] ❌ Invalid board snapshot');
            this.showMessage('Неверные данные сохранения', 'error');
            return;
        }

        // Если нет presetSnapshot (старые сохранения), считаем все ячейки пользовательскими
        const preset = presetSnapshot || '0'.repeat(81);

        this.cells.forEach((cell, index) => {
            const value = boardSnapshot[index];
            const isPreset = preset[index] === '1';

            if (value !== '0') {
                cell.value = value;
                cell.readOnly = isPreset;
                if (isPreset) {
                    cell.classList.add('preset');
                } else {
                    cell.classList.remove('preset');
                }
            } else {
                cell.value = '';
                cell.readOnly = false;
                cell.classList.remove('preset');
            }

            cell.classList.remove('error', 'valid');
        });

        this.updateValidateButtonState();
    }

    async showSavedGames() {
        console.log('[SUDOKU] Loading saved games...');

        try {
            const response = await fetch('/api/savegame');
            const data = await response.json();

            console.log('[SUDOKU] Saved games response:', { status: response.status, data });

            if (response.ok) {
                this.displaySavedGamesModal(data);
            } else {
                throw new Error(data.error || 'Не удалось загрузить сохранения');
            }
        } catch (error) {
            console.error('[SUDOKU] ❌ Error loading saved games:', error);
            this.showMessage(`Ошибка загрузки сохранений: ${error.message}`, 'error');
        }
    }

    displaySavedGamesModal(savedGames) {
        console.log('[SUDOKU] Displaying saved games modal:', savedGames);

        try {
            const modal = document.getElementById('savedGamesModal');
            if (!modal) {
                throw new Error('Saved games modal element not found');
            }

            const savedGamesList = document.getElementById('savedGamesList');
            if (!savedGamesList) {
                throw new Error('Saved games list element not found');
            }

            if (savedGames.length === 0) {
                savedGamesList.innerHTML = '<div class="no-saves">Нет сохраненных игр</div>';
            } else {
                let savedGamesHtml = '';
                savedGames.forEach((save, index) => {
                    savedGamesHtml += `
                    <div class="saved-game-item">
                        <div class="save-info">
                            <div class="save-title">Сохранение #${save.saveId}</div>
                            <div class="save-details">
                                <span class="save-level">${save.level}</span>
                                <span class="save-time">${save.formattedTime}</span>
                            </div>
                        </div>
                        <div class="save-actions">
                            <button class="load-btn" onclick="window.sudokuGame.loadSavedGame(${save.saveId})">
                                Загрузить
                            </button>
                            <button class="delete-btn" onclick="window.sudokuGame.deleteSavedGame(${save.saveId})">
                                Удалить
                            </button>
                        </div>
                    </div>
                `;
                });
                savedGamesList.innerHTML = savedGamesHtml;
            }

            modal.style.display = 'block';
            console.log('[SUDOKU] ✅ Saved games modal displayed');

        } catch (error) {
            console.error('[SUDOKU] ❌ Error displaying saved games modal:', error);
            this.showMessage('Ошибка отображения сохранений', 'error');
        }
    }

    async deleteSavedGame(saveId) {
        console.log('[SUDOKU] Deleting saved game:', saveId);

        if (!confirm('Вы уверены, что хотите удалить это сохранение?')) {
            return;
        }

        try {
            const response = await fetch(`/api/savegame/${saveId}`, {
                method: 'DELETE'
            });

            const data = await response.json();
            console.log('[SUDOKU] Delete response:', { status: response.status, data });

            if (response.ok) {
                this.showMessage('Сохранение удалено', 'success');
                // Обновляем список сохранений
                this.showSavedGames();
            } else {
                throw new Error(data.error || 'Не удалось удалить сохранение');
            }
        } catch (error) {
            console.error('[SUDOKU] ❌ Error deleting save:', error);
            this.showMessage(`Ошибка удаления: ${error.message}`, 'error');
        }
    }

    renderGrid(seed) {
        console.log('[SUDOKU] Rendering grid with seed:', {
            seedLength: seed ? seed.length : 'undefined',
            seed: seed ? seed.substring(0, 20) + '...' : 'null'
        });

        if (!seed || seed.length !== 81) {
            console.error('[SUDOKU] ❌ Invalid seed data');
            this.showMessage('Неверные данные игры', 'error');
            return;
        }

        let presetCount = 0;
        let emptyCount = 0;

        this.cells.forEach((cell, index) => {
            const value = seed[index];

            if (value !== '0') {
                cell.value = value;
                cell.readOnly = true;
                cell.classList.add('preset');
                presetCount++;
            } else {
                cell.value = '';
                cell.readOnly = false;
                cell.classList.remove('preset', 'error', 'valid');
                emptyCount++;
            }

            cell.classList.remove('error', 'valid');
        });

        console.log('[SUDOKU] ✅ Grid rendered:', { presetCount, emptyCount });
        this.updateValidateButtonState();
    }

    handleCellInput(event, index) {
        const cell = event.target;
        let value = cell.value;

        // Разрешаем только цифры 1-9
        if (value && (!/^[1-9]$/.test(value))) {
            cell.value = '';
            return;
        }

        cell.classList.remove('error', 'valid');
        this.updateValidateButtonState();
    }

    updateValidateButtonState() {
        const canValidate = this.currentGame && !this.isGameSolved;
        this.validateBtn.disabled = !canValidate;
    }

    validateCurrentState() {
        console.log('[SUDOKU] Validating current state...');

        if (!this.currentGame || this.isGameSolved) {
            console.log('[SUDOKU] ⚠️ Cannot validate - no game or already solved');
            return;
        }

        const currentState = this.getCurrentGameState();
        console.log('[SUDOKU] Current state length:', currentState.length);

        if (!this.isGameComplete()) {
            this.showMessage('Заполните все ячейки для валидации', 'warning');
            return;
        }

        if (this.isValidSudoku(currentState)) {
            console.log('[SUDOKU] ✅ Sudoku solution is valid!');
            this.showMessage('Поздравляем! Головоломка решена правильно!', 'success');
            this.isGameSolved = true;
            this.disableAllCells();
            this.saveGameStats();
        } else {
            console.log('[SUDOKU] ❌ Sudoku solution is invalid');
            this.showMessage('Решение неверное. Проверьте правила судоку.', 'error');
        }

        this.updateValidateButtonState();
    }

    async solvePuzzle() {
        console.log('[SUDOKU] Solving puzzle...');

        if (!this.currentGame) {
            this.showMessage('Пожалуйста, сначала начните новую игру', 'warning');
            return;
        }

        if (this.isGameSolved) {
            this.showMessage('Игра уже решена', 'info');
            return;
        }

        this.showMessage('Решение головоломки...', 'info');

        try {
            // всегда проверяем что есть seed или puzzleId
            let requestBody = {};

            if (this.currentGame.seed) {
                requestBody.seed = this.currentGame.seed;
            }

            if (this.currentGame.id) {
                requestBody.puzzleId = this.currentGame.id;
            }

            // убеждаемся что передан хотя бы один идентификатор
            if (!requestBody.seed && !requestBody.puzzleId) {
                throw new Error('No seed or puzzle ID available');
            }

            console.log('[SUDOKU] Solve request body:', requestBody);

            const response = await fetch('/api/solve', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });

            const data = await response.json();
            console.log('[SUDOKU] Solve response:', { status: response.status, data });

            if (response.ok) {
                this.renderGrid(data.solution);
                this.isGameSolved = true;
                this.disableAllCells();
                this.showMessage('Головоломка решена автоматически!', 'success');
                console.log('[SUDOKU] ✅ Puzzle solved successfully');
            } else {
                throw new Error(data.error || 'Не удалось решить головоломку');
            }
        } catch (error) {
            console.error('[SUDOKU] ❌ Error solving puzzle:', error);
            this.showMessage(`Ошибка решения: ${error.message}`, 'error');
        }
    }

    getCurrentGameState() {
        let state = '';
        this.cells.forEach((cell) => {
            const value = cell.value || '0';
            state += value;
        });
        return state;
    }

    isValidSudoku(board) {
        // Проверяем строки
        for (let row = 0; row < 9; row++) {
            const seen = new Set();
            for (let col = 0; col < 9; col++) {
                const value = board[row * 9 + col];
                if (value !== '0') {
                    if (seen.has(value)) return false;
                    seen.add(value);
                }
            }
        }

        // Проверяем столбцы
        for (let col = 0; col < 9; col++) {
            const seen = new Set();
            for (let row = 0; row < 9; row++) {
                const value = board[row * 9 + col];
                if (value !== '0') {
                    if (seen.has(value)) return false;
                    seen.add(value);
                }
            }
        }

        // Проверяем 3x3 блоки
        for (let blockRow = 0; blockRow < 3; blockRow++) {
            for (let blockCol = 0; blockCol < 3; blockCol++) {
                const seen = new Set();
                for (let row = 0; row < 3; row++) {
                    for (let col = 0; col < 3; col++) {
                        const value = board[(blockRow * 3 + row) * 9 + (blockCol * 3 + col)];
                        if (value !== '0') {
                            if (seen.has(value)) return false;
                            seen.add(value);
                        }
                    }
                }
            }
        }

        return true;
    }

    isGameComplete() {
        const filledCells = Array.from(this.cells).filter(cell => cell.value).length;
        return filledCells === 81;
    }

    enableAllCells() {
        this.cells.forEach(cell => {
            if (!cell.classList.contains('preset')) {
                cell.readOnly = false;
            }
        });
    }

    disableAllCells() {
        this.cells.forEach(cell => {
            cell.readOnly = true;
        });
    }

    showMessage(text, type = 'info') {
        console.log(`[SUDOKU] Message: [${type}] ${text}`);

        this.messageDiv.textContent = text;
        this.messageDiv.className = `message ${type}`;

        setTimeout(() => {
            if (this.messageDiv.textContent === text) {
                this.messageDiv.textContent = '';
                this.messageDiv.className = 'message';
            }
        }, 5000);
    }

    async saveGameStats() {
        console.log('[SUDOKU] Saving game stats...');

        if (!this.currentGame || !this.gameStartTime) {
            console.error('[SUDOKU] ❌ Cannot save stats - missing data');
            return;
        }

        const duration = Math.floor((Date.now() - this.gameStartTime) / 1000);
        const statsData = {
            puzzleId: this.currentGame.id.toString(),
            duration: duration
        };

        console.log('[SUDOKU] Saving stats:', statsData);

        try {
            // Точно такой же запрос, как в диагностике
            const response = await fetch('/api/stats', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(statsData)
            });

            const data = await response.json();
            console.log('[SUDOKU] Save stats response:', { status: response.status, data });

            if (response.ok) {
                console.log('[SUDOKU] ✅ Game stats saved successfully');
                this.showMessage(`Результат сохранен! Время: ${this.formatDuration(duration)}`, 'success');
            } else {
                console.error('[SUDOKU] ❌ Failed to save game stats:', data);
                this.showMessage('Ошибка сохранения результата: ' + (data.error || 'Unknown error'), 'error');
            }
        } catch (error) {
            console.error('[SUDOKU] ❌ Error in saveGameStats:', error);
            this.showMessage('Ошибка сохранения результата: ' + error.message, 'error');
        }
    }

    async showGameHistory() {
        console.log('[SUDOKU] Loading game statistics...');

        try {
            // Точно такой же запрос, как в диагностике
            const response = await fetch('/api/stats');
            const data = await response.json();

            console.log('[SUDOKU] Stats response:', { status: response.status, data });

            if (response.ok) {
                this.displayStatsModal(data);
            } else {
                throw new Error(data.error || 'Не удалось загрузить статистику');
            }
        } catch (error) {
            console.error('[SUDOKU] ❌ Error loading statistics:', error);
            this.showMessage(`Ошибка загрузки статистики: ${error.message}`, 'error');
        }
    }

    displayStatsModal(data) {
        console.log('[SUDOKU] Displaying stats modal:', data);

        try {
            const modal = document.getElementById('statsModal');
            if (!modal) {
                throw new Error('Stats modal element not found');
            }

            // Заполняем общую статистику
            const totalStatsDiv = document.getElementById('totalStats');
            if (totalStatsDiv && data.totalStats) {
                totalStatsDiv.innerHTML = `
                    <div class="stat-item">
                        <strong>Total Games:</strong> ${data.totalStats.totalGames}
                    </div>
                    <div class="stat-item">
                        <strong>Average Time:</strong> ${this.formatDuration(Math.floor(data.totalStats.avgTime))}
                    </div>
                    <div class="stat-item">
                        <strong>Best Time:</strong> ${this.formatDuration(data.totalStats.bestTime)}
                    </div>
                `;
            }

            // Заполняем статистику по уровням
            const levelStatsDiv = document.getElementById('levelStats');
            if (levelStatsDiv && data.levelStats) {
                let levelStatsHtml = '';
                for (const [level, stats] of Object.entries(data.levelStats)) {
                    levelStatsHtml += `
                        <div class="level-stat">
                            <h4>${level}</h4>
                            <div class="stat-details">
                                <div>Games: ${stats.gamesPlayed}</div>
                                <div>Avg Time: ${this.formatDuration(Math.floor(stats.avgTime))}</div>
                                <div>Best Time: ${this.formatDuration(stats.bestTime)}</div>
                            </div>
                        </div>
                    `;
                }
                levelStatsDiv.innerHTML = levelStatsHtml;
            }

            // Заполняем историю игр
            const gameHistoryDiv = document.getElementById('gameHistory');
            if (gameHistoryDiv && data.gameHistory) {
                let historyHtml = '';
                data.gameHistory.forEach((game, index) => {
                    historyHtml += `
                        <div class="history-item">
                            <div class="game-number">#${index + 1}</div>
                            <div class="game-level">${game.level}</div>
                            <div class="game-duration">${game.formattedDuration}</div>
                        </div>
                    `;
                });
                gameHistoryDiv.innerHTML = historyHtml;
            }

            // Показываем модальное окно
            modal.style.display = 'block';
            console.log('[SUDOKU] ✅ Stats modal displayed');

        } catch (error) {
            console.error('[SUDOKU] ❌ Error displaying stats modal:', error);
            this.showMessage('Ошибка отображения статистики', 'error');
        }
    }

    setupModal() {
        try {
            // Настройка модального окна статистики
            const statsModal = document.getElementById('statsModal');
            if (statsModal) {
                const statsCloseBtn = statsModal.querySelector('.close');
                if (statsCloseBtn) {
                    statsCloseBtn.onclick = () => {
                        statsModal.style.display = 'none';
                    };
                }
            }

            // Настройка модального окна сохранённых игр
            const savedGamesModal = document.getElementById('savedGamesModal');
            if (savedGamesModal) {
                const savedGamesCloseBtn = savedGamesModal.querySelector('.close');
                if (savedGamesCloseBtn) {
                    savedGamesCloseBtn.onclick = () => {
                        savedGamesModal.style.display = 'none';
                    };
                }
            }

            // Закрытие модальных окон по клику вне их области
            window.onclick = (event) => {
                if (event.target === statsModal) {
                    statsModal.style.display = 'none';
                }
                if (event.target === savedGamesModal) {
                    savedGamesModal.style.display = 'none';
                }
            };

            console.log('[SUDOKU] ✅ Modal setup completed');
        } catch (error) {
            console.error('[SUDOKU] ❌ Error setting up modal:', error);
        }
    }

    formatDuration(seconds) {
        if (seconds === 0) return '0:00';

        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const secs = seconds % 60;

        if (hours > 0) {
            return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
        } else {
            return `${minutes}:${secs.toString().padStart(2, '0')}`;
        }
    }
}

// Инициализация игры при загрузке страницы
document.addEventListener('DOMContentLoaded', () => {
    console.log('[SUDOKU] DOM Content Loaded - initializing game...');

    try {
        const game = new SudokuGame();

        // Добавляем глобальную ссылку для отладки
        window.sudokuGame = game;
        console.log('[SUDOKU] ✅ Game initialized successfully. Access via window.sudokuGame');

        // Глобальные функции отладки
        window.sudokuDebug = {
            toggleDebug: () => {
                game.debugMode = !game.debugMode;
                console.log(`[SUDOKU] Debug mode ${game.debugMode ? 'enabled' : 'disabled'}`);
            },
            getCurrentState: () => game.getCurrentGameState(),
            validateState: () => game.validateCurrentState(),
            getGameInfo: () => ({
                currentGame: game.currentGame,
                isGameSolved: game.isGameSolved,
                gameStartTime: game.gameStartTime
            }),
            // Новые функции для тестирования как в диагностике
            testNewGame: async () => {
                console.log('[SUDOKU] Testing new game...');
                await game.startNewGame();
            },
            testStats: async () => {
                console.log('[SUDOKU] Testing stats...');
                await game.showGameHistory();
            },
            testSolve: async () => {
                console.log('[SUDOKU] Testing solve...');
                await game.solvePuzzle();
            }
        };

        console.log('[SUDOKU] ✅ Debug utilities available at window.sudokuDebug');

    } catch (error) {
        console.error('[SUDOKU] ❌ Failed to initialize game:', error);
    }
});