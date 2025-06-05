package org.example.sudoku.service;

public interface BoardFacade {
    void putNumber(int row, int col, int value);
    void clearCell(int row, int col);
    void requestHint();
    void solveCompletely();
}