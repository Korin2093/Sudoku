package org.example.sudoku.service;

public class SolverServiceImpl implements SolverService {

    @Override
    public int countSolutions(String boardSnapshot) {
        int[][] board = toMatrix(boardSnapshot);
        return countSolutionsBacktrack(board, 0, 0, 0);
    }

    // Новый приватный метод для подсчета решений
    private static int countSolutionsBacktrack(int[][] b, int r, int c, int solutionCount) {
        if (solutionCount >= 2) return solutionCount; // оптимизация: если уже найдено 2+ решения, останавливаемся

        if (r == 9) return solutionCount + 1; // нашли одно решение

        if (b[r][c] != 0) // клетка уже заполнена
            return countSolutionsBacktrack(b, nextR(r, c), nextC(c), solutionCount);

        for (int n = 1; n <= 9; n++) {
            if (safe(b, r, c, n)) {
                b[r][c] = n;
                solutionCount = countSolutionsBacktrack(b, nextR(r, c), nextC(c), solutionCount);
                b[r][c] = 0; // откат

                if (solutionCount >= 2) return solutionCount; // прерываем, если уже найдено 2+ решения
            }
        }
        return solutionCount;
    }

    // boardSnapshot — 81 символ (‘0’ — пустая клетка)
    @Override
    public String solve(String boardSnapshot) {
        int[][] board = toMatrix(boardSnapshot);
        if (solveBacktrack(board, 0, 0)) {
            return toString(board);
        }
        throw new IllegalStateException("Board is unsolvable");
    }

    @Override
    public boolean isValid(String boardSnapshot) {
        try {
            return solve(boardSnapshot) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /* ───────────── helpers ───────────── */

    private static boolean solveBacktrack(int[][] b, int r, int c) {
        if (r == 9) return true;                // дошли до конца
        if (b[r][c] != 0)                       // клетка уже заполнена
            return solveBacktrack(b, nextR(r, c), nextC(c));

        for (int n = 1; n <= 9; n++) {
            if (safe(b, r, c, n)) {
                b[r][c] = n;
                if (solveBacktrack(b, nextR(r, c), nextC(c))) return true;
                b[r][c] = 0;                    // откат
            }
        }
        return false;                           // ни одно число не подошло
    }

    /* ───────────── utils ───────────── */

    private static int nextR(int r, int c) { return c == 8 ? r + 1 : r; }
    private static int nextC(int c)         { return c == 8 ? 0     : c + 1; }

    private static boolean safe(int[][] b, int r, int c, int n) {
        for (int i = 0; i < 9; i++)
            if (b[r][i] == n || b[i][c] == n) return false;      // строка/столбец
        int rs = r / 3 * 3, cs = c / 3 * 3;                      // 3×3 блок
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (b[rs + i][cs + j] == n) return false;
        return true;
    }

    private static int[][] toMatrix(String s) {
        if (s.length() != 81) throw new IllegalArgumentException("Must be 81 chars");
        int[][] m = new int[9][9];
        for (int i = 0; i < 81; i++) m[i / 9][i % 9] = s.charAt(i) - '0';
        return m;
    }

    private static String toString(int[][] m) {
        StringBuilder sb = new StringBuilder(81);
        for (int[] row : m) for (int n : row) sb.append(n);
        return sb.toString();
    }
}
