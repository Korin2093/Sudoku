package org.example.sudoku.domain;

public record Puzzle(int id, String seed, Level level, int[][] grid) {}