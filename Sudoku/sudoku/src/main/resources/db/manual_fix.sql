-- Manual database fix script
-- Run this SQL command directly on your SQLite database if the migration doesn't work

-- Add the missing column to the savegame table
ALTER TABLE savegame ADD COLUMN preset_snapshot TEXT;

-- Verify the column was added
PRAGMA table_info(savegame);
