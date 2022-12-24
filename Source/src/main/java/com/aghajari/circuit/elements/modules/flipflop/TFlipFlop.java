package com.aghajari.circuit.elements.modules.flipflop;

public class TFlipFlop extends BaseFlipFlop {

    public TFlipFlop() {
        super();
    }

    protected String getModuleName() {
        return "T-FlipFlop";
    }

    @Override
    protected boolean calculate(
            boolean oldEnabled,
            boolean newEnabled,
            boolean d,
            boolean prevState
    ) {
        return (!oldEnabled && newEnabled) ? d != prevState : prevState;
    }
}
