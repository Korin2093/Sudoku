package org.example.sudoku.service;

import org.example.sudoku.domain.Level;

import java.util.*;

/**
 * Генерирует случайную задачу Sudoku с уникальным решением.
 * Возвращает строку-снимок из 81 символа (0 — пустая клетка).
 */
public final class PuzzleGenerator {
    private static final Random RND = new Random();

    private PuzzleGenerator() { }

    /* ────────────── API ────────────── */

    public static String generate(Level level) {
        int clues = switch (level) {          // сколько цифр оставить
            case EASY   -> 36;
            case MEDIUM -> 32;
            case HARD   -> 28;
        };

        SolverService solver = new SolverServiceImpl(); // создаем экземпляр решателя
        String puzzle;
        int attempts = 0;
        int maxAttempts = 1000; // ограничиваем количество попыток

        do {
            int[][] full = new int[9][9];
            fillFullBoard(full, 0, 0);            // заполняем полностью

            char[] chars = new char[81];
            int k = 0;
            for (int[] row : full)                // превращаем в строку
                for (int n : row) chars[k++] = (char) ('0' + n);

            // удаляем случайные клетки, пока не останется clues
            int toRemove = 81 - clues;
            while (toRemove > 0) {
                int idx = RND.nextInt(81);
                if (chars[idx] != '0') {
                    chars[idx] = '0';
                    toRemove--;
                }
            }
            puzzle = new String(chars);
            attempts++;

            if (attempts >= maxAttempts) {
                throw new RuntimeException("Could not generate a puzzle with unique solution after " + maxAttempts + " attempts");
            }

        } while (solver.countSolutions(puzzle) != 1); // повторяем, пока не найдем головоломку с единственным решением

        return puzzle;
    }

    /* ────────────── helpers ────────────── */

    private static boolean fillFullBoard(int[][] b, int r, int c) {
        if (r == 9) return true;
        if (b[r][c] != 0) return fillFullBoard(b, nextR(r, c), nextC(c));

        List<Integer> nums = new ArrayList<>(List.of(1,2,3,4,5,6,7,8,9));
        Collections.shuffle(nums, RND);
        for (int n : nums) {
            if (safe(b, r, c, n)) {
                b[r][c] = n;
                if (fillFullBoard(b, nextR(r, c), nextC(c))) return true;
                b[r][c] = 0;                  // откат
            }
        }
        return false;
    }

    private static int nextR(int r, int c) { return c == 8 ? r + 1 : r; }
    private static int nextC(int c)        { return c == 8 ? 0     : c + 1; }

    /** Проверка: можно ли поставить n в клетку (r,c) */
    private static boolean safe(int[][] b, int r, int c, int n) {
        for (int i = 0; i < 9; i++)
            if (b[r][i] == n || b[i][c] == n) return false;     // строка / столбец
        int rs = r / 3 * 3, cs = c / 3 * 3;                     // 3×3-блок
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (b[rs + i][cs + j] == n) return false;
        return true;
    }
}
