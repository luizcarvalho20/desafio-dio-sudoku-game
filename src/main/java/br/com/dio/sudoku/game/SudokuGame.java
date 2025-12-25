package br.com.dio.sudoku.game;

import br.com.dio.sudoku.board.Board;
import br.com.dio.sudoku.board.Space;
import br.com.dio.sudoku.util.ArgsParser;
import br.com.dio.sudoku.game.SudokuValidator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Scanner;

public class SudokuGame {

    private final Board board = new Board();
    private final Scanner sc = new Scanner(System.in);
    private boolean started = false;

    // undo: guarda jogadas (col,row,valorAnterior,notasAnterior)
    private final Deque<Move> history = new ArrayDeque<>();

    public SudokuGame(String[] args) {
        ArgsParser.applyInitialSpaces(board, args);
    }

    public void run() {
        while (true) {
            printMenu();
            String op = readLine("Escolha uma opção: ");

            if (isQuit(op)) {
                System.out.println("Encerrando... até mais!");
                return;
            }

            switch (op) {
                case "1" -> startNewGame();
                case "2" -> placeNumber();
                case "3" -> removeNumber();
                case "4" -> viewGame();
                case "5" -> statusGame();
                case "6" -> clearUser();
                case "7" -> finishGame();
                case "8" -> undoMove();
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== SUDOKU (Terminal) ===");
        System.out.println("1. Iniciar novo jogo");
        System.out.println("2. Colocar um novo número");
        System.out.println("3. Remover um número");
        System.out.println("4. Verificar jogo (mostrar tabuleiro)");
        System.out.println("5. Verificar status do jogo");
        System.out.println("6. Limpar (remove números do usuário, mantém fixos)");
        System.out.println("7. Finalizar o jogo");
        System.out.println("8. Voltar uma jogada (UNDO)");
        System.out.println("0. Sair (a qualquer momento)");
        System.out.println("Dica: digite 0, sair, exit ou quit quando quiser.");
        System.out.println();
    }

    private void startNewGame() {
        started = true;
        history.clear();
        System.out.println("Jogo iniciado!");
        printBoard();
    }

    private void placeNumber() {
        if (!ensureStarted()) return;

        String numStr = readLine("Número (1-9) ou 0 para sair: ");
        if (isQuit(numStr)) return;

        Integer value = parseInt(numStr);
        if (value == null || value < 1 || value > 9) {
            System.out.println("Número inválido.");
            return;
        }

        Integer col = askIndex("Índice horizontal (col 0-8): ");
        if (col == null) return;

        Integer row = askIndex("Índice vertical (row 0-8): ");
        if (row == null) return;

        if (!board.isInside(col, row)) {
            System.out.println("Posição fora do tabuleiro.");
            return;
        }

        Space space = board.getSpace(col, row);
        if (space.isFixed()) {
            System.out.println("Não pode alterar um número fixo.");
            return;
        }
        if (space.getValue() != null) {
            System.out.println("Essa posição já está preenchida. Remova antes para trocar.");
            return;
        }

        // salva estado anterior para UNDO
        history.push(Move.from(col, row, space));

        board.setUserValue(col, row, value);
        printBoard();
        warnIfConflict();
    }

    private void removeNumber() {
        if (!ensureStarted()) return;

        Integer col = askIndex("Índice horizontal (col 0-8) ou 0 para sair: ", true);
        if (col == null) return;

        Integer row = askIndex("Índice vertical (row 0-8) ou 0 para sair: ", true);
        if (row == null) return;

        if (!board.isInside(col, row)) {
            System.out.println("Posição fora do tabuleiro.");
            return;
        }

        Space space = board.getSpace(col, row);
        if (space.isFixed()) {
            System.out.println("Esse número é fixo e não pode ser removido.");
            return;
        }

        if (space.getValue() == null) {
            System.out.println("Essa posição já está vazia.");
            return;
        }

        // salva para UNDO
        history.push(Move.from(col, row, space));

        board.setUserValue(col, row, null);
        printBoard();
        warnIfConflict();
    }

    private void viewGame() {
        if (!ensureStarted()) return;
        printBoard();
        warnIfConflict();
    }

    private void statusGame() {
        if (!started) {
            System.out.println("Status: NÃO INICIADO (sempre sem erro).");
            return;
        }

        boolean hasError = SudokuValidator.hasConflicts(board);

        String status;
        if (board.isEmptyAllNonFixed()) status = "NÃO INICIADO";
        else if (board.isFullyFilled()) status = "COMPLETO";
        else status = "INCOMPLETO";

        System.out.println("Status: " + status);
        System.out.println("Erros: " + (hasError ? "SIM (há conflitos)" : "NÃO"));
    }

    private void clearUser() {
        if (!ensureStarted()) return;
        board.clearUserInputs();
        history.clear(); // limpa histórico porque já não faz sentido desfazer após limpar tudo
        System.out.println("Números do usuário removidos (fixos mantidos).");
        printBoard();
    }

    private void finishGame() {
        if (!ensureStarted()) return;

        if (!board.isFullyFilled()) {
            System.out.println("Ainda existem espaços vazios. Preencha todos para finalizar.");
            return;
        }

        if (SudokuValidator.hasConflicts(board)) {
            System.out.println("O tabuleiro está completo, mas contém erros (conflitos). Corrija antes de finalizar.");
            return;
        }

        System.out.println("Parabéns! Sudoku completo e válido ✅");
        printBoard();
        System.out.println("Jogo encerrado.");
        System.exit(0);
    }

    private void undoMove() {
        if (!ensureStarted()) return;

        if (history.isEmpty()) {
            System.out.println("Nada para desfazer.");
            return;
        }

        Move last = history.pop();
        Space space = board.getSpace(last.col, last.row);

        if (space.isFixed()) {
            System.out.println("Não é possível desfazer em uma posição fixa (estranho, mas ok).");
            return;
        }

        // restaura valor anterior
        board.setUserValue(last.col, last.row, last.prevValue);

        // restaura notas anteriores (se você usar notas depois)
        space.getNotes().clear();
        space.getNotes().addAll(last.prevNotes);

        System.out.println("Última jogada desfeita.");
        printBoard();
        warnIfConflict();
    }

    // =========================
    // Helpers
    // =========================

    private boolean ensureStarted() {
        if (!started) {
            System.out.println("Você ainda não iniciou o jogo. Use a opção 1.");
            return false;
        }
        return true;
    }

    private void warnIfConflict() {
        if (SudokuValidator.hasConflicts(board)) {
            System.out.println("⚠ Atenção: há conflitos no tabuleiro!");
        }
    }

    private String readLine(String msg) {
        System.out.print(msg);
        String s = sc.nextLine();
        return s == null ? "" : s.trim();
    }

    private boolean isQuit(String s) {
        if (s == null) return false;
        s = s.trim().toLowerCase(Locale.ROOT);
        return s.equals("0") || s.equals("sair") || s.equals("exit") || s.equals("quit");
    }

    private Integer askIndex(String prompt) {
        return askIndex(prompt, false);
    }

    private Integer askIndex(String prompt, boolean allowQuitZero) {
        String s = readLine(prompt);
        if (allowQuitZero && isQuit(s)) return null; // volta pro menu sem erro
        if (isQuit(s)) return null;

        Integer v = parseInt(s);
        if (v == null || v < 0 || v > 8) {
            System.out.println("Índice inválido. Use 0 a 8.");
            return null;
        }
        return v;
    }

    private Integer parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void printBoard() {
        System.out.println();
        System.out.println("    0 1 2   3 4 5   6 7 8");
        System.out.println("  +-------+-------+-------+");
        for (int row = 0; row < Board.SIZE; row++) {
            System.out.print(row + " | ");
            for (int col = 0; col < Board.SIZE; col++) {
                Space space = board.getSpace(col, row);
                String v = (space.getValue() == null) ? "." : String.valueOf(space.getValue());
                System.out.print(v + " ");
                if (col % 3 == 2) System.out.print("| ");
            }
            System.out.println();
            if (row % 3 == 2) {
                System.out.println("  +-------+-------+-------+");
            }
        }
        System.out.println();
    }

    // =========================
    // Move (para UNDO)
    // =========================
    private static class Move {
        final int col;
        final int row;
        final Integer prevValue;
        final java.util.Set<Integer> prevNotes;

        private Move(int col, int row, Integer prevValue, java.util.Set<Integer> prevNotes) {
            this.col = col;
            this.row = row;
            this.prevValue = prevValue;
            this.prevNotes = prevNotes;
        }

        static Move from(int col, int row, Space space) {
            return new Move(col, row, space.getValue(), new java.util.HashSet<>(space.getNotes()));
        }
    }
}
