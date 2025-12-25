package br.com.dio.sudoku.util;

import br.com.dio.sudoku.board.Board;

public class ArgsParser {

    // formato: "col,row;value,fixed"
    // exemplo: "0,0;4,false"
    public static void applyInitialSpaces(Board board, String[] args) {
        if (args == null) return;

        for (String token : args) {
            if (token == null) continue;
            token = token.trim();
            if (token.isEmpty()) continue;

            // "0,0;4,false"
            String[] parts = token.split(";");
            if (parts.length != 2) continue;

            String[] pos = parts[0].split(",");
            if (pos.length != 2) continue;

            String[] data = parts[1].split(",");
            if (data.length != 2) continue;

            int col = Integer.parseInt(pos[0].trim());
            int row = Integer.parseInt(pos[1].trim());
            int value = Integer.parseInt(data[0].trim());
            boolean fixed = Boolean.parseBoolean(data[1].trim());

            if (!board.isInside(col, row)) continue;

            if (fixed) board.setFixed(col, row, value);
            else board.setUserValue(col, row, value);
        }
    }
}
