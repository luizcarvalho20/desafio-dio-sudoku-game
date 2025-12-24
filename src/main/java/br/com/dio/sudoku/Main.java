package br.com.dio.sudoku;

import br.com.dio.sudoku.game.SudokuGame;

public class Main {
    public static void main(String[] args) {
        SudokuGame game = new SudokuGame(args);
        game.run();
    }
}
