package com.aghajari.circuit.elements.modules.flipflop;

public class NegativeTFlipFlop extends TFlipFlop {

    public NegativeTFlipFlop() {
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
        return (oldEnabled && !newEnabled) ? d != prevState : prevState;
    }
}
