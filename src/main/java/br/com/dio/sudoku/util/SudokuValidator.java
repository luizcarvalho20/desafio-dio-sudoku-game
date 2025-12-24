package br.com.dio.sudoku.util;

import br.com.dio.sudoku.board.Board;

public class SudokuValidator {

    public static boolean hasConflicts(Board board) {
        // qualquer repetição (ignorando null) em linha/coluna/quadrante => conflito
        for (int i = 0; i < Board.SIZE; i++) {
            if (hasRowConflict(board, i)) return true;
            if (hasColConflict(board, i)) return true;
        }

        for (int boxRow = 0; boxRow < 3; boxRow++) {
            for (int boxCol = 0; boxCol < 3; boxCol++) {
                if (hasBoxConflict(board, boxCol * 3, boxRow * 3)) return true;
            }
        }
        return false;
    }

    private static boolean hasRowConflict(Board board, int row) {
        boolean[] seen = new boolean[10]; // 1..9
        for (int col = 0; col < Board.SIZE; col++) {
            Integer v = board.getSpace(col, row).getValue();
            if (v == null) continue;
            if (seen[v]) return true;
            seen[v] = true;
        }
        return false;
    }

    private static boolean hasColConflict(Board board, int col) {
        boolean[] seen = new boolean[10];
        for (int row = 0; row < Board.SIZE; row++) {
            Integer v = board.getSpace(col, row).getValue();
            if (v == null) continue;
            if (seen[v]) return true;
            seen[v] = true;
        }
        return false;
    }

    private static boolean hasBoxConflict(Board board, int startCol, int startRow) {
        boolean[] seen = new boolean[10];
        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                Integer v = board.getSpace(c, r).getValue();
                if (v == null) continue;
                if (seen[v]) return true;
                seen[v] = true;
            }
        }
        return false;
    }
}
