package com.aghajari.circuit.elements.modules;

import com.aghajari.circuit.connector.InputConnector;
import com.aghajari.circuit.connector.OutputConnector;
import com.aghajari.circuit.elements.BaseGate;

import java.util.List;

public class DLatch extends BaseGate {

    private String elementName;
    private boolean prevState;

    public DLatch() {
        drawRectAsPath();
        initialize();
    }

    @Override
    protected int getInputCount(int count) {
        return 2;
    }

    @Override
    protected int getOutputCount() {
        return 2;
    }

    @Override
    protected boolean isTextOnCenter() {
        return true;
    }

    private void initModuleText() {
        text.setText(elementName + "\n(D-Latch)");
    }

    @Override
    public void setElementName(String name) {
        elementName = name;
        initModuleText();
    }

    @Override
    public String getElementName() {
        return elementName;
    }

    @Override
    public boolean calculate(OutputConnector output) {
        int index = getAllOutputConnectors().indexOf(output);

        List<InputConnector> inputs = getAllInputConnectors();
        boolean enabled = inputs.get(1).calculate();

        if (enabled) {
            prevState = inputs.get(0).calculate();
        }
        return (index == 0) == prevState;
    }
}
