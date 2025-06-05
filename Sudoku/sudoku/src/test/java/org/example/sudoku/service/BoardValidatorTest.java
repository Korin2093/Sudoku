package org.example.sudoku.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BoardValidatorTest {

    private static final String BOARD =
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
    void canPlaceDigit() {
        // в (0,4) сейчас 7 — туда нельзя поставить 5
        Assertions.assertFalse(BoardValidator.canPlace(BOARD, 0, 4, 5));
        // в (0,2) пусто — 2 поставить можно
        Assertions.assertTrue(BoardValidator.canPlace(BOARD, 0, 2, 2));
    }
}
