package br.com.dio.sudoku.ui;

import br.com.dio.sudoku.board.Board;
import br.com.dio.sudoku.board.Space;
import br.com.dio.sudoku.game.SudokuValidator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;

public class SudokuFrame extends JFrame {

    private final Board board;
    private final JTextField[][] fields = new JTextField[Board.SIZE][Board.SIZE];

    private final Deque<Move> history = new ArrayDeque<>();
    private boolean programmaticChange = false; // evita registrar "undo" quando a gente atualiza a UI via código

    public SudokuFrame(Board board) {
        super("Sudoku");
        this.board = board;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildGridPanel(), BorderLayout.CENTER);
        add(buildButtonsPanel(), BorderLayout.SOUTH);

        refreshAllFieldsFromBoard();
        highlightConflicts();
    }

    private JPanel buildGridPanel() {
        JPanel gridPanel = new JPanel(new GridLayout(9, 9));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Font font = new Font("Arial", Font.BOLD, 18);

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                JTextField tf = new JTextField();
                tf.setHorizontalAlignment(SwingConstants.CENTER);
                tf.setFont(font);

                Space space = board.getSpace(col, row);

                if (space.isFixed()) {
                    tf.setEditable(false);
                    tf.setBackground(new Color(235, 235, 235));
                } else {
                    tf.setEditable(true);
                    tf.setBackground(Color.WHITE);
                }

                // Bordas 3x3
                int top = (row % 3 == 0) ? 3 : 1;
                int left = (col % 3 == 0) ? 3 : 1;
                int bottom = (row == 8) ? 3 : 1;
                int right = (col == 8) ? 3 : 1;
                tf.setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, Color.GRAY));

                int finalRow = row;
                int finalCol = col;

                tf.getDocument().addDocumentListener(new DocumentListener() {
                    @Override public void insertUpdate(DocumentEvent e) { onChange(finalCol, finalRow); }
                    @Override public void removeUpdate(DocumentEvent e) { onChange(finalCol, finalRow); }
                    @Override public void changedUpdate(DocumentEvent e) { onChange(finalCol, finalRow); }
                });

                fields[row][col] = tf;
                gridPanel.add(tf);
            }
        }
        return gridPanel;
    }

    private JPanel buildButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton btnReset = new JButton("Reiniciar jogo");
        JButton btnCheck = new JButton("Verificar jogo");
        JButton btnFinish = new JButton("Concluir");
        JButton btnUndo = new JButton("Desfazer");
        JButton btnExit = new JButton("Sair");

        btnReset.addActionListener(e -> {
            board.clearUserInputs();
            history.clear();
            refreshAllFieldsFromBoard();
            highlightConflicts();
            JOptionPane.showMessageDialog(this, "Jogo reiniciado (mantendo os números fixos).");
        });

        btnCheck.addActionListener(e -> {
            boolean conflicts = SudokuValidator.hasConflicts(board);
            boolean filled = board.isFullyFilled();
            String status = filled ? "COMPLETO" : "INCOMPLETO";

            String msg = "Status: " + status + "\n"
                    + "Erros (conflitos): " + (conflicts ? "SIM" : "NÃO");

            highlightConflicts();
            JOptionPane.showMessageDialog(this, msg);
        });

        btnFinish.addActionListener(e -> {
            boolean conflicts = SudokuValidator.hasConflicts(board);
            boolean filled = board.isFullyFilled();

            highlightConflicts();

            if (!filled) {
                JOptionPane.showMessageDialog(this, "Ainda faltam espaços para preencher.");
                return;
            }
            if (conflicts) {
                JOptionPane.showMessageDialog(this, "Seu jogo tem conflitos. Corrija antes de concluir.");
                return;
            }

            JOptionPane.showMessageDialog(this, "Parabéns! Sudoku concluído com sucesso!");
            dispose(); // fecha a janela
        });

        btnUndo.addActionListener(e -> undoLastMove());

        btnExit.addActionListener(e -> {
            int opt = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja sair do jogo agora?",
                    "Sair",
                    JOptionPane.YES_NO_OPTION
            );
            if (opt == JOptionPane.YES_OPTION) dispose();
        });

        panel.add(btnReset);
        panel.add(btnCheck);
        panel.add(btnFinish);
        panel.add(btnUndo);
        panel.add(btnExit);

        return panel;
    }

    private void onChange(int col, int row) {
        if (programmaticChange) return;

        Space space = board.getSpace(col, row);
        if (space.isFixed()) return;

        String text = fields[row][col].getText().trim();

        // Normaliza: se o usuário colar várias coisas, só considera 1 char
        if (text.length() > 1) {
            text = text.substring(0, 1);
            programmaticChange = true;
            fields[row][col].setText(text);
            programmaticChange = false;
        }

        Integer oldValue = space.getValue();
        Integer newValue = parseDigitOrNull(text);

        // Se digitou inválido, volta pro valor anterior
        if (text.isEmpty()) {
            newValue = null;
        } else if (newValue == null) {
            // inválido: desfaz visualmente
            programmaticChange = true;
            fields[row][col].setText(oldValue == null ? "" : String.valueOf(oldValue));
            programmaticChange = false;
            return;
        }

        if ((oldValue == null && newValue == null) || (oldValue != null && oldValue.equals(newValue))) {
            highlightConflicts();
            return;
        }

        // Registra histórico pra Undo
        history.push(new Move(col, row, oldValue, newValue));

        // Aplica no board
        board.setUserValue(col, row, newValue);

        highlightConflicts();
    }

    private Integer parseDigitOrNull(String s) {
        if (s == null || s.isEmpty()) return null;
        char c = s.charAt(0);
        if (c < '1' || c > '9') return null;
        return c - '0';
    }

    private void undoLastMove() {
        if (history.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nada para desfazer.");
            return;
        }

        Move last = history.pop();
        board.setUserValue(last.col, last.row, last.oldValue);

        programmaticChange = true;
        fields[last.row][last.col].setText(last.oldValue == null ? "" : String.valueOf(last.oldValue));
        programmaticChange = false;

        highlightConflicts();
    }

    private void refreshAllFieldsFromBoard() {
        programmaticChange = true;
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                Space s = board.getSpace(col, row);
                fields[row][col].setText(s.getValue() == null ? "" : String.valueOf(s.getValue()));
            }
        }
        programmaticChange = false;
    }

    private void highlightConflicts() {
        // reseta cores
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                Space s = board.getSpace(col, row);
                if (s.isFixed()) fields[row][col].setBackground(new Color(235, 235, 235));
                else fields[row][col].setBackground(Color.WHITE);
            }
        }

        // pinta conflitos
        boolean[][] conflicts = SudokuValidator.conflictMatrix(board);
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (conflicts[row][col]) {
                    fields[row][col].setBackground(new Color(255, 200, 200)); // vermelho claro
                }
            }
        }
    }

    private static class Move {
        final int col, row;
        final Integer oldValue;
        final Integer newValue;

        Move(int col, int row, Integer oldValue, Integer newValue) {
            this.col = col;
            this.row = row;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }
}
