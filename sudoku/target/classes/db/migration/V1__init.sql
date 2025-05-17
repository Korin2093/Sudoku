CREATE TABLE puzzle(
                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                       seed TEXT NOT NULL,
                       level TEXT NOT NULL
);
CREATE TABLE savegame(
                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                         puzzle_id INTEGER,
                         board TEXT NOT NULL,
                         seconds INT,
                         FOREIGN KEY(puzzle_id) REFERENCES puzzle(id)
);
CREATE TABLE stats(
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      puzzle_id INTEGER,
                      duration INT,
                      solved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      FOREIGN KEY(puzzle_id) REFERENCES puzzle(id)
);
