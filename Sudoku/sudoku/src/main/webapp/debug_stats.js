// Отладочный скрипт для проверки статистики
// Добавьте этот код в консоль браузера для отладки

async function debugStats() {
    console.log('=== ОТЛАДКА СТАТИСТИКИ SUDOKU ===');
    
    try {
        // Проверяем получение статистики
        console.log('1. Проверка получения статистики...');
        const response = await fetch('/api/stats');
        const data = await response.json();
        
        console.log('Status:', response.status);
        console.log('Response:', data);
        
        if (response.ok) {
            console.log('✅ Статистика получена успешно');
            console.log('Всего игр:', data.totalStats.totalGames);
            console.log('История игр:', data.gameHistory.length);
            
            if (data.totalStats.totalGames === 0) {
                console.log('⚠️ База данных пуста - нет сохраненных игр');
                
                // Пробуем создать тестовую игру
                console.log('2. Создание тестовой игры...');
                const newGameResponse = await fetch('/api/new?level=EASY');
                const gameData = await newGameResponse.json();
                
                if (newGameResponse.ok) {
                    console.log('✅ Тестовая игра создана:', gameData);
                    
                    // Пробуем сохранить тестовую статистику
                    console.log('3. Сохранение тестовой статистики...');
                    const saveResponse = await fetch('/api/stats', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            puzzleId: gameData.id.toString(),
                            duration: 120
                        })
                    });
                    
                    const saveData = await saveResponse.json();
                    console.log('Save Status:', saveResponse.status);
                    console.log('Save Response:', saveData);
                    
                    if (saveResponse.ok) {
                        console.log('✅ Тестовая статистика сохранена');
                        
                        // Проверяем еще раз
                        const checkResponse = await fetch('/api/stats');
                        const checkData = await checkResponse.json();
                        console.log('4. Повторная проверка:', checkData.totalStats.totalGames, 'игр');
                    } else {
                        console.error('❌ Ошибка сохранения тестовой статистики');
                    }
                } else {
                    console.error('❌ Ошибка создания тестовой игры');
                }
            } else {
                console.log('✅ База данных содержит игры - проблема в другом');
            }
        } else {
            console.error('❌ Ошибка получения статистики');
        }
        
    } catch (error) {
        console.error('❌ Критическая ошибка:', error);
    }
}

// Запускаем отладку
debugStats();
