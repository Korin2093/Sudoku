package org.example.sudoku.domain;

public record GameState(int id, Puzzle puzzle, String boardSnapshot, int elapsedSeconds) {}