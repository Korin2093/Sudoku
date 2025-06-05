package org.example.sudoku.domain;

public record GameState(int id, Puzzle puzzle, String boardSnapshot, String presetSnapshot, int elapsedSeconds) {}