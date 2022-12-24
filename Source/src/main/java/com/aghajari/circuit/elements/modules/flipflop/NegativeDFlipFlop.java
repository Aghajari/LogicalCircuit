package com.aghajari.circuit.elements.modules.flipflop;

public class NegativeDFlipFlop extends DFlipFlop {

    public NegativeDFlipFlop() {
        super();
    }

    @Override
    protected boolean isNegative() {
        return true;
    }

    @Override
    protected boolean calculate(
            boolean oldEnabled,
            boolean newEnabled,
            boolean d,
            boolean prevState
    ) {
        return (oldEnabled && !newEnabled) ? d : prevState;
    }
}
