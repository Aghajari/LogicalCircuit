package com.aghajari.circuit;

import java.util.List;

public class TruthTable {

    private final List<String> wires;
    private final List<List<Boolean>> truthTable;

    TruthTable(List<String> wires, List<List<Boolean>> truthTable) {
        this.wires = wires;
        this.truthTable = truthTable;
    }

    public List<String> getWires() {
        return wires;
    }

    public List<List<Boolean>> getTruthTable() {
        return truthTable;
    }
}
