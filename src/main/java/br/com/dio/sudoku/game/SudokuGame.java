package br.com.dio.sudoku.game;

import br.com.dio.sudoku.board.Board;
import br.com.dio.sudoku.board.Space;
import br.com.dio.sudoku.util.ArgsParser;
import br.com.dio.sudoku.util.BoardPrinter;
import br.com.dio.sudoku.util.SudokuValidator;

import java.util.Scanner;

public class SudokuGame {

    private final Board board = new Board();
    private final String[] args;
    private boolean started = false;

    public SudokuGame(String[] args) {
        this.args = args;
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
                default -> System.out.println("Opção inválida. Escolha de 1 a 7.\n");
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
        System.out.print("Escolha: ");
    }

    // (1) iniciar novo jogo
    private void startNewGame() {
        board.clearUserInputs(); // limpa entradas anteriores
        // também zera tudo e aplica args (inclui fixos)
        // para simplificar: recria fixos aplicando em cima do board atual
        // (como fixos não podem ser apagados pelo clearUserInputs, este start funciona
        // se você rodar o jogo pela primeira vez; se quiser reiniciar várias vezes,
        // rode novamente o programa)
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

