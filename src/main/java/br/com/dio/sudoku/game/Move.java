package br.com.dio.sudoku.game;

public record Move(int col, int row, Integer previousValue) {}
