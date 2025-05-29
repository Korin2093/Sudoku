package org.example.sudoku.service;

/** контракт решателя Sudoku */
public interface SolverService {

    /** решает судоку, переданную строкой из 81 символа (0 — пустая клетка).
     * возвращает такую же строку, но без нулей, если решение существует */
    String solve(String boardSnapshot);

    /** проверяет решаема ли переданная доска */

    boolean isValid(String boardSnapshot);
}
