package br.com.dio.sudoku.ui;

import br.com.dio.sudoku.board.Board;
import br.com.dio.sudoku.board.Space;
import br.com.dio.sudoku.game.SudokuValidator;



import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.Stack;

public class MainScreen extends JFrame {

    private final Board board;
    private final NumberTextField[][] fields = new NumberTextField[9][9];

    private final Stack<Move> history = new Stack<>();

    public MainScreen(Board board) {
        this.board = board;

        setTitle("Sudoku");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        setLayout(new BorderLayout(10, 10));
        add(buildGrid(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        refreshFromBoard();

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildGrid() {
        JPanel grid = new JPanel(new GridLayout(9, 9));
        grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Font font = new Font("SansSerif", Font.BOLD, 18);

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {

                NumberTextField tf = new NumberTextField();
                tf.setFont(font);
                tf.setPreferredSize(new Dimension(45, 45));

                // bordas mais grossas a cada 3x3
                int top = (row % 3 == 0) ? 3 : 1;
                int left = (col % 3 == 0) ? 3 : 1;
                int bottom = (row == 8) ? 3 : 1;
                int right = (col == 8) ? 3 : 1;

                // também fecha bloco 3x3
                if ((row + 1) % 3 == 0) bottom = 3;
                if ((col + 1) % 3 == 0) right = 3;

                tf.setBorder(new MatteBorder(top, left, bottom, right, Color.GRAY));

                final int r = row;
                final int c = col;

                // Quando o usuário digitar, tenta aplicar no board
                tf.addActionListener(e -> applyUserInput(c, r));
                tf.addFocusListener(new java.awt.event.FocusAdapter() {
                    @Override
                    public void focusLost(java.awt.event.FocusEvent e) {
                        applyUserInput(c, r);
                    }
                });

                fields[row][col] = tf;
                grid.add(tf);
            }
        }

        return grid;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton resetBtn = new JButton("Reiniciar jogo");
        JButton checkBtn = new JButton("Verificar jogo");
        JButton finishBtn = new JButton("Concluir");
        JButton undoBtn = new JButton("Desfazer");
        JButton exitBtn = new JButton("Sair");

        resetBtn.addActionListener(e -> {
            board.clearUserInputs();
            history.clear();
            refreshFromBoard();
            showInfo("Jogo reiniciado! Mantive apenas os números fixos.");
        });

        checkBtn.addActionListener(e -> {
            boolean conflicts = SudokuValidator.hasConflicts(board);
            if (conflicts) showError("Há conflitos no tabuleiro (erros).");
            else showInfo("Sem conflitos até agora ✅");
        });

        finishBtn.addActionListener(e -> {
            if (!board.isFullyFilled()) {
                showError("Ainda falta preencher espaços.");
                return;
            }
            boolean conflicts = SudokuValidator.hasConflicts(board);
            if (conflicts) {
                showError("O tabuleiro está completo, mas contém conflitos.");
                return;
            }
            showInfo("Parabéns! Sudoku completo e válido ✅");
            dispose();
        });

        undoBtn.addActionListener(e -> undoLastMove());

        exitBtn.addActionListener(e -> dispose());

        panel.add(resetBtn);
        panel.add(checkBtn);
        panel.add(finishBtn);
        panel.add(undoBtn);
        panel.add(exitBtn);

        return panel;
    }

    private void applyUserInput(int col, int row) {
        Space space = board.getSpace(col, row);
        NumberTextField tf = fields[row][col];

        if (space.isFixed()) {
            tf.setNumber(space.getValue());
            return;
        }

        Integer newValue = tf.getNumberOrNull();
        Integer oldValue = space.getValue();

        // se não mudou, não faz nada
        if ((oldValue == null && newValue == null) || (oldValue != null && oldValue.equals(newValue))) {
            return;
        }

        // salva no histórico p/ desfazer
        history.push(new Move(col, row, oldValue));

        board.setUserValue(col, row, newValue);

        // feedback visual simples: pinta em vermelho se tiver conflito
        refreshConflictColors();
    }

    private void undoLastMove() {
        if (history.isEmpty()) {
            showInfo("Não há jogadas para desfazer.");
            return;
        }

        Move last = history.pop();
        Space space = board.getSpace(last.col, last.row);

        if (space.isFixed()) {
            showError("Não dá pra desfazer uma posição fixa.");
            return;
        }

        board.setUserValue(last.col, last.row, last.previousValue);
        refreshFromBoard();
    }

    private void refreshFromBoard() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                Space s = board.getSpace(col, row);
                NumberTextField tf = fields[row][col];

                tf.setNumber(s.getValue());

                if (s.isFixed()) {
                    tf.setEditable(false);
                    tf.setBackground(new Color(235, 235, 235));
                } else {
                    tf.setEditable(true);
                    tf.setBackground(Color.WHITE);
                }
                tf.setForeground(Color.BLACK);
            }
        }
        refreshConflictColors();
    }

    private void refreshConflictColors() {
        boolean conflicts = SudokuValidator.hasConflicts(board);

        // se tiver conflito, a gente só colore os não-fixos com vermelho quando existe conflito geral
        // (simples e suficiente pro desafio; depois dá pra melhorar e marcar célula por célula)
        if (conflicts) {
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    Space s = board.getSpace(col, row);
                    if (!s.isFixed() && s.getValue() != null) {
                        fields[row][col].setForeground(Color.RED.darker());
                    }
                }
            }
        } else {
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    fields[row][col].setForeground(Color.BLACK);
                }
            }
        }
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Sudoku", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Sudoku", JOptionPane.ERROR_MESSAGE);
    }

    private static class Move {
        final int col;
        final int row;
        final Integer previousValue;

        Move(int col, int row, Integer previousValue) {
            this.col = col;
            this.row = row;
            this.previousValue = previousValue;
        }
    }
}
