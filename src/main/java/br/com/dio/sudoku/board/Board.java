package br.com.dio.sudoku.board;

public class Board {

    public static final int SIZE = 9;

    private final Space[][] grid = new Space[SIZE][SIZE];

    public Board() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col] = new Space(null, false);
            }
        }
    }

    public boolean isInside(int col, int row) {
        return col >= 0 && col < SIZE && row >= 0 && row < SIZE;
    }

    public Space getSpace(int col, int row) {
        return grid[row][col];
    }

    public void setFixed(int col, int row, int value) {
        grid[row][col] = new Space(value, true);
    }

    public void setUserValue(int col, int row, Integer value) {
        Space current = grid[row][col];
        if (current.isFixed()) {
            return;
        }
        current.setValue(value);
        current.getNotes().clear();
    }

    public void clearUserInputs() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Space s = grid[row][col];
                if (!s.isFixed()) {
                    s.setValue(null);
                    s.getNotes().clear();
                }
            }
        }
    }

    public boolean isEmptyAllNonFixed() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Space s = grid[row][col];
                if (!s.isFixed() && s.getValue() != null) return false;
            }
        }
        return true;
    }

    public boolean isFullyFilled() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (grid[row][col].getValue() == null) return false;
            }
        }
        return true;
    }
}
