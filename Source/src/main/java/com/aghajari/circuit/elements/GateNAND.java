package com.aghajari.circuit.elements;

import com.aghajari.circuit.connector.OutputConnector;

public class GateNAND extends GateAND {

    public GateNAND() {
        super();
    }

    @Override
    protected boolean isNotGate() {
        return true;
    }

    @Override
    public boolean calculate(OutputConnector connector) {
        return !super.calculate(connector);
    }
}
