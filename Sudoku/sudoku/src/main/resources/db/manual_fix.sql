-- ручное исправление базы данных
-- запустите эти команды в sqlite, если миграция не сработала

-- добавляем недостающую колонку в таблицу savegame
ALTER TABLE savegame ADD COLUMN preset_snapshot TEXT;

-- проверяем, что колонка добавилась
PRAGMA table_info(savegame);
