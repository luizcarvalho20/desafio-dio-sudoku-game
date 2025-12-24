package br.com.dio.sudoku.game;

import br.com.dio.sudoku.board.Board;
import br.com.dio.sudoku.board.Space;
import br.com.dio.sudoku.util.ArgsParser;
import br.com.dio.sudoku.util.BoardPrinter;
import br.com.dio.sudoku.util.SudokuValidator;

import java.util.Scanner;
import java.util.Stack;

public class SudokuGame {

    private final Board board = new Board();
    private final String[] args;

    private boolean started = false;

    // histórico para desfazer última jogada
    private final Stack<Move> history = new Stack<>();

    public SudokuGame(String[] args) {
        this.args = (args == null) ? new String[0] : args;
    }

    public void run() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            printMenu();
            String option = sc.nextLine().trim();

            switch (option) {
                case "1" -> startNewGame();
                case "2" -> placeNumber(sc);
                case "3" -> removeNumber(sc);
                case "4" -> viewGame();
                case "5" -> checkStatus();
                case "6" -> clearUserNumbers();
                case "7" -> {
                    if (finishGame()) return;
                }
                case "8" -> undoLastMove();
                case "0" -> {
                    System.out.println("\nJogo encerrado pelo usuário. Até mais!");
                    return;
                }
                default -> System.out.println("Opção inválida. Escolha uma opção do menu.\n");
            }
        }
    }

    private void printMenu() {
        System.out.println("===== SUDOKU (Terminal) =====");
        System.out.println("1) Iniciar um novo jogo");
        System.out.println("2) Colocar um novo número");
        System.out.println("3) Remover um número");
        System.out.println("4) Verificar jogo (visualizar tabuleiro)");
        System.out.println("5) Verificar status do jogo");
        System.out.println("6) Limpar (remover números do usuário)");
        System.out.println("7) Finalizar o jogo");
        System.out.println("8) Desfazer última jogada");
        System.out.println("0) Encerrar o jogo");
        System.out.print("Escolha: ");
    }

    // (1) iniciar novo jogo
    private void startNewGame() {
        // limpa entradas anteriores do usuário
        board.clearUserInputs();

        // limpa histórico
        history.clear();

        // aplica valores iniciais pelos args
        ArgsParser.applyInitialSpaces(board, args);

        started = true;

        System.out.println("\nJogo iniciado com os valores passados por args!\n");
        BoardPrinter.print(board);
    }

    // (2) colocar número
    private void placeNumber(Scanner sc) {
        if (!ensureStarted()) return;

        int value = readInt(sc, "Número (1-9): ");
        int col = readInt(sc, "Índice horizontal (col 0-8): ");
        int row = readInt(sc, "Índice vertical (row 0-8): ");

        if (!board.isInside(col, row)) {
            System.out.println("Posição fora do tabuleiro.\n");
            return;
        }
        if (value < 1 || value > 9) {
            System.out.println("Número inválido. Use 1 a 9.\n");
            return;
        }

        Space space = board.getSpace(col, row);

        if (space.isFixed()) {
            System.out.println("Essa posição é fixa (número inicial). Não pode alterar.\n");
            return;
        }
        if (space.getValue() != null) {
            System.out.println("Essa posição já está preenchida. Remova antes de colocar outro.\n");
            return;
        }

        // salva estado anterior para undo
        history.push(new Move(col, row, space.getValue()));

        board.setUserValue(col, row, value);
        System.out.println("Número inserido.\n");
        BoardPrinter.print(board);
    }

    // (3) remover número
    private void removeNumber(Scanner sc) {
        if (!ensureStarted()) return;

        int col = readInt(sc, "Índice horizontal (col 0-8): ");
        int row = readInt(sc, "Índice vertical (row 0-8): ");

        if (!board.isInside(col, row)) {
            System.out.println("Posição fora do tabuleiro.\n");
            return;
        }

        Space space = board.getSpace(col, row);

        if (space.isFixed()) {
            System.out.println("Esse número é FIXO e não pode ser removido.\n");
            return;
        }

        if (space.getValue() == null) {
            System.out.println("Essa posição já está vazia.\n");
            return;
        }

        // salva estado anterior para undo
        history.push(new Move(col, row, space.getValue()));

        board.setUserValue(col, row, null);
        System.out.println("Número removido.\n");
        BoardPrinter.print(board);
    }

    // (4) visualizar
    private void viewGame() {
        if (!ensureStarted()) return;
        BoardPrinter.print(board);
    }

    // (5) status + erros
    private void checkStatus() {
        if (!started) {
            System.out.println("\nStatus: NÃO INICIADO");
            System.out.println("Erros: NÃO (status não iniciado é sempre sem erro)\n");
            return;
        }

        GameStatus status = getCurrentStatus();
        boolean hasErrors = SudokuValidator.hasConflicts(board);

        System.out.println("\nStatus: " + statusToPt(status));
        System.out.println("Erros: " + (hasErrors ? "SIM (há conflitos)" : "NÃO"));
        System.out.println();
    }

    // (6) limpar entradas do usuário
    private void clearUserNumbers() {
        if (!ensureStarted()) return;

        board.clearUserInputs();
        history.clear();

        System.out.println("Números do usuário removidos. Fixos mantidos.\n");
        BoardPrinter.print(board);
    }

    // (7) finalizar
    private boolean finishGame() {
        if (!ensureStarted()) return false;

        GameStatus status = getCurrentStatus();
        boolean hasErrors = SudokuValidator.hasConflicts(board);

        if (status == GameStatus.COMPLETE && !hasErrors) {
            System.out.println("\nParabéns! Sudoku completo e válido. Jogo encerrado ✅");
            return true;
        }

        System.out.println("\nAinda não dá pra finalizar!");
        if (status != GameStatus.COMPLETE) {
            System.out.println("- Você precisa preencher todos os espaços.");
        }
        if (hasErrors) {
            System.out.println("- O tabuleiro contém conflitos (erros).");
        }
        System.out.println();
        return false;
    }

    // (8) desfazer última jogada
    private void undoLastMove() {
        if (!ensureStarted()) return;

        if (history.isEmpty()) {
            System.out.println("\nNão há jogadas para desfazer.\n");
            return;
        }

        Move last = history.pop();

        // se a posição for fixa (não deveria acontecer), apenas ignora
        if (board.getSpace(last.col(), last.row()).isFixed()) {
            System.out.println("\nNão é possível desfazer em uma posição fixa.\n");
            return;
        }

        board.setUserValue(last.col(), last.row(), last.previousValue());
        System.out.println("\nÚltima jogada desfeita.\n");
        BoardPrinter.print(board);
    }

    private GameStatus getCurrentStatus() {
        if (!started) return GameStatus.NOT_STARTED;
        if (board.isFullyFilled()) return GameStatus.COMPLETE;
        return GameStatus.INCOMPLETE;
    }

    private String statusToPt(GameStatus status) {
        return switch (status) {
            case NOT_STARTED -> "NÃO INICIADO";
            case INCOMPLETE -> "INCOMPLETO";
            case COMPLETE -> "COMPLETO";
        };
    }

    private boolean ensureStarted() {
        if (!started) {
            System.out.println("\nVocê ainda não iniciou o jogo. Use a opção 1.\n");
            return false;
        }
        return true;
    }

    private int readInt(Scanner sc, String label) {
        while (true) {
            System.out.print(label);
            String s = sc.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Digite um número válido.\n");
            }
        }
    }
}
