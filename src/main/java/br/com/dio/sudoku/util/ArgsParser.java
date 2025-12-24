package br.com.dio.sudoku.util;

import br.com.dio.sudoku.board.Board;

public class ArgsParser {

    // Formato esperado (separado por espaço):
    // col,row;value,fixed
    // Ex: 0,0;4,false
    public static void applyInitialSpaces(Board board, String[] args) {
        if (args == null || args.length == 0) return;

        for (String token : args) {
            if (token == null || token.isBlank()) continue;

            // token: "0,0;4,false"
            String[] parts = token.trim().split(";");
            if (parts.length != 2) continue;

            String[] pos = parts[0].split(",");
            if (pos.length != 2) continue;

            String[] data = parts[1].split(",");
            if (data.length != 2) continue;

            try {
                int col = Integer.parseInt(pos[0].trim());
                int row = Integer.parseInt(pos[1].trim());
                int value = Integer.parseInt(data[0].trim());
                boolean fixed = Boolean.parseBoolean(data[1].trim());

                if (!board.isInside(col, row)) continue;
                if (value < 1 || value > 9) continue;

                if (fixed) {
                    board.setFixed(col, row, value);
                } else {
                    board.setUserValue(col, row, value);
                }
            } catch (NumberFormatException ignored) {
                // ignora entradas inválidas
            }
        }
    }
}
