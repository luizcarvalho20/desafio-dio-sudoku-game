package br.com.dio.sudoku.board;

import java.util.HashSet;
import java.util.Set;

public class Space {

    private Integer value;           // null = vazio
    private final boolean fixed;     // true = número inicial (não pode remover/substituir)
    private final Set<Integer> notes = new HashSet<>(); // extra (rascunho)

    public Space(Integer value, boolean fixed) {
        this.value = value;
        this.fixed = fixed;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public boolean isFixed() {
        return fixed;
    }

    public Set<Integer> getNotes() {
        return notes;
    }
}
