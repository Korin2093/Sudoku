package org.example.sudoku.service;

/** контракт решателя Sudoku */
public interface SolverService {
    /** подсчитывает количество решений для переданной доски.
     * возвращает количество найденных решений (обычно ограничивается 2+ для оптимизации) */
    int countSolutions(String boardSnapshot);

    /** решает судоку, переданную строкой из 81 символа (0 — пустая клетка).
     * возвращает такую же строку, но без нулей, если решение существует */
    String solve(String boardSnapshot);

    /** проверяет решаема ли переданная доска */

    boolean isValid(String boardSnapshot);
}
