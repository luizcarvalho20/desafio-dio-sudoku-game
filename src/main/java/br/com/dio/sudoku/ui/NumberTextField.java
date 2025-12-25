package br.com.dio.sudoku.ui;

import javax.swing.*;
import javax.swing.text.*;

public class NumberTextField extends JTextField {

    public NumberTextField() {
        super(1);
        setHorizontalAlignment(JTextField.CENTER);
        ((AbstractDocument) getDocument()).setDocumentFilter(new OneDigit1to9Filter());
    }

    public Integer getNumberOrNull() {
        String t = getText().trim();
        if (t.isEmpty()) return null;
        try {
            int v = Integer.parseInt(t);
            return (v >= 1 && v <= 9) ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void setNumber(Integer n) {
        setText(n == null ? "" : String.valueOf(n));
    }

    private static class OneDigit1to9Filter extends DocumentFilter {
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {

            if (text == null) return;

            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String next = new StringBuilder(current).replace(offset, offset + length, text).toString();
            next = next.trim();

            // Permitir limpar
            if (next.isEmpty()) {
                super.replace(fb, offset, length, text, attrs);
                return;
            }

            // Permitir apenas 1 dígito 1..9
            if (next.matches("[1-9]")) {
                fb.replace(0, fb.getDocument().getLength(), next, attrs);
            }
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            replace(fb, offset, 0, string, attr);
        }
    }
}
