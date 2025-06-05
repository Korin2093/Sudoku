package org.example.sudoku.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SolverServiceTest {

    SolverService solver = new SolverServiceImpl();

    // пример лёгкой доски (0 — пусто)
    private static final String EASY =
            "530070000" +
                    "600195000" +
                    "098000060" +
                    "800060003" +
                    "400803001" +
                    "700020006" +
                    "060000280" +
                    "000419005" +
                    "000080079";

    @Test
    void solveEasyBoard() {
        String solved = solver.solve(EASY);
        Assertions.assertEquals(81, solved.length());
        Assertions.assertFalse(solved.contains("0"));
    }

    @Test
    void boardIsValid() {
        Assertions.assertTrue(solver.isValid(EASY));
    }
}
