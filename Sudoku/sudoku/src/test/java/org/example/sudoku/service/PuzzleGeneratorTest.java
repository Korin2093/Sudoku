package org.example.sudoku.service;

import org.example.sudoku.domain.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

class PuzzleGeneratorTest {

    @RepeatedTest(3) // проверяем несколько раз (меньше повторений из-за более долгой генерации)
    void generatedBoardHasUniqueSolution() {
        String puzzle = PuzzleGenerator.generate(Level.EASY);
        SolverService solver = new SolverServiceImpl();

        int solutionCount = solver.countSolutions(puzzle);
        Assertions.assertEquals(1, solutionCount, "Generated puzzle should have exactly one solution");
    }

    @RepeatedTest(5) // сгенерируем 5 разных задач
    void generatedBoardIsSolvable() {
        String puzzle = PuzzleGenerator.generate(Level.MEDIUM);
        String solution = new SolverServiceImpl().solve(puzzle);

        // решение такое же по длине, не содержит нулей
        Assertions.assertEquals(81, solution.length());
        Assertions.assertFalse(solution.contains("0"));
    }
}
