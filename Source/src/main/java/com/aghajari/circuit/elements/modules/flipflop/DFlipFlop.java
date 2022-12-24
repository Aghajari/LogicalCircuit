package com.aghajari.circuit.elements.modules.flipflop;

public class DFlipFlop extends BaseFlipFlop {

    public DFlipFlop() {
        super();
    }

    protected String getModuleName() {
        return "D-FlipFlop";
    }

    @Override
    protected boolean calculate(
            boolean oldEnabled,
            boolean newEnabled,
            boolean d,
            boolean prevState
    ) {
        return (!oldEnabled && newEnabled) ? d : prevState;
    }
}
