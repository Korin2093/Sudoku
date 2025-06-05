package org.example.sudoku.service;

/** проверяет можно ли поставить цифру в выбранную клетку 9×9 */
public final class BoardValidator {

    private BoardValidator() {}

    /**
     * @param board 81 символ (0 — пусто)
     * @param row   0…8
     * @param col   0…8
     * @param digit 1…9
     */
    public static boolean canPlace(String board, int row, int col, int digit) {
        if (digit < 1 || digit > 9) return false;
        if (board.charAt(row * 9 + col) != '0') return false;

        for (int i = 0; i < 9; i++) {
            // проверка строки и столбца
            if (board.charAt(row * 9 + i) == digit + '0') return false;
            if (board.charAt(i * 9 + col) == digit + '0') return false;
        }
        // проверка 3×3 блока
        int rs = (row / 3) * 3, cs = (col / 3) * 3;
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (board.charAt((rs + r) * 9 + cs + c) == digit + '0')
                    return false;
        return true;
    }
}
