package br.com.dio.sudoku.util;

import br.com.dio.sudoku.board.Board;
import br.com.dio.sudoku.board.Space;

public class BoardPrinter {

    public static void print(Board board) {
        System.out.println();
        System.out.println("    0 1 2   3 4 5   6 7 8");
        System.out.println("  +-------+-------+-------+");

        for (int row = 0; row < Board.SIZE; row++) {
            System.out.print(row + " | ");
            for (int col = 0; col < Board.SIZE; col++) {
                Space space = board.getSpace(col, row);
                String v = (space.getValue() == null) ? "." : String.valueOf(space.getValue());
                System.out.print(v + " ");

                if ((col + 1) % 3 == 0) System.out.print("| ");
            }
            System.out.println();
            if ((row + 1) % 3 == 0) {
                System.out.println("  +-------+-------+-------+");
            }
        }
        System.out.println();
        System.out.println("Legenda: '.' = vazio | índices: col,row (0 a 8)");
        System.out.println();
    }
}
