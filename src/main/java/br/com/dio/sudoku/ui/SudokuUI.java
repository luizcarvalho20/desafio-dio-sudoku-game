package br.com.dio.sudoku.ui;

import br.com.dio.sudoku.board.Board;
import br.com.dio.sudoku.util.ArgsParser;

import javax.swing.*;

public class SudokuUI {

    public static void start(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Board board = new Board();
            ArgsParser.applyInitialSpaces(board, args); // usa os args igual no terminal
            MainScreen screen = new MainScreen(board);
            screen.setVisible(true);
        });
    }
}
