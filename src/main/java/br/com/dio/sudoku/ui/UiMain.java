package br.com.dio.sudoku.ui;

import br.com.dio.sudoku.board.Board;
import br.com.dio.sudoku.util.ArgsParser;

import javax.swing.SwingUtilities;

public class UiMain {
    public static void main(String[] args) {
        Board board = new Board();
        ArgsParser.applyInitialSpaces(board, args);

        SwingUtilities.invokeLater(() -> {
            SudokuFrame frame = new SudokuFrame(board);
            frame.setVisible(true);
        });
    }
}
