package br.com.dio.sudoku.game;

import br.com.dio.sudoku.board.Board;
import br.com.dio.sudoku.board.Space;

public class SudokuValidator {

    public static boolean hasConflicts(Board board) {
        boolean[][] m = conflictMatrix(board);
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (m[r][c]) return true;
            }
        }
        return false;
    }

    // Matriz [row][col] indicando quais posições estão em conflito
    public static boolean[][] conflictMatrix(Board board) {
        boolean[][] conflict = new boolean[9][9];

        // checar linhas
        for (int row = 0; row < 9; row++) {
            for (int c1 = 0; c1 < 9; c1++) {
                Integer v1 = get(board, c1, row);
                if (v1 == null) continue;
                for (int c2 = c1 + 1; c2 < 9; c2++) {
                    Integer v2 = get(board, c2, row);
                    if (v1.equals(v2)) {
                        conflict[row][c1] = true;
                        conflict[row][c2] = true;
                    }
                }
            }
        }

        // checar colunas
        for (int col = 0; col < 9; col++) {
            for (int r1 = 0; r1 < 9; r1++) {
                Integer v1 = get(board, col, r1);
                if (v1 == null) continue;
                for (int r2 = r1 + 1; r2 < 9; r2++) {
                    Integer v2 = get(board, col, r2);
                    if (v1.equals(v2)) {
                        conflict[r1][col] = true;
                        conflict[r2][col] = true;
                    }
                }
            }
        }

        // checar blocos 3x3
        for (int br = 0; br < 3; br++) {
            for (int bc = 0; bc < 3; bc++) {
                int startRow = br * 3;
                int startCol = bc * 3;

                for (int i = 0; i < 9; i++) {
                    int col1 = startCol + (i % 3);
                    int row1 = startRow + (i / 3);
                    Integer v1 = get(board, col1, row1);
                    if (v1 == null) continue;

                    for (int j = i + 1; j < 9; j++) {
                        int col2 = startCol + (j % 3);
                        int row2 = startRow + (j / 3);
                        Integer v2 = get(board, col2, row2);

                        if (v1.equals(v2)) {
                            conflict[row1][col1] = true;
                            conflict[row2][col2] = true;
                        }
                    }
                }
            }
        }

        return conflict;
    }

    private static Integer get(Board board, int col, int row) {
        Space s = board.getSpace(col, row);
        return s.getValue();
    }
}
