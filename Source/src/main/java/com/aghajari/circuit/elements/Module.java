package com.aghajari.circuit.elements;

import com.aghajari.circuit.CircuitBoard;
import com.aghajari.circuit.connector.InputConnector;
import com.aghajari.circuit.connector.OutputConnector;

import java.util.ArrayList;
import java.util.List;

public class Module extends BaseGate {

    private CircuitBoard board;
    private String elementName;

    private int inputCount = 1;
    private int outputCount = 1;

    public Module() {
        drawRectAsPath();
        initialize();
        setPrefHeight(60);
        requestUpdate();

        prefHeightProperty().addListener((observableValue, number, t1) -> requestUpdate());
    }

    private void analyzeBoard() {
        if (board == null) {
            inputCount = outputCount = 1;
            return;
        }

        int oldSize = Math.max(inputCount, outputCount) + 1;
        inputCount = outputCount = 0;

        board.getElementsInOrder().forEach(element -> {
            if (element instanceof Wire) {
                Wire wire = (Wire) element;
                if (wire.isInternalGateWire()) return;

                if (!wire.getInputConnector().hasConnected())
                    inputCount++;

                if (!wire.getOutputConnector().hasConnected())
                    outputCount++;
            }
        });

        outputCount = Math.max(1, outputCount);

        int newSize = Math.max(inputCount, outputCount) + 1;
        if (newSize != oldSize)
            setPrefHeight(newSize * 30);
    }

    @Override
    public void requestUpdate() {
        analyzeBoard();
        super.requestUpdate();
    }

    @Override
    protected int getInputCount(int count) {
        return inputCount;
    }

    @Override
    protected int getOutputCount() {
        return outputCount;
    }

    @Override
    protected boolean isTextOnCenter() {
        return true;
    }

    public CircuitBoard getModuleBoard() {
        return board;
    }

    public void setModuleBoard(CircuitBoard board) {
        this.board = board;
        initModuleText();
        requestUpdate();
    }

    private void initModuleText() {
        if (board != null) {
            text.setText(elementName + "\n(" + board.getBoardName() + ")");
        } else {
            text.setText(elementName);
        }
    }

    public void requestUpdateText() {
        initModuleText();
        requestUpdate();
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
        int indexOfOutput = getAllOutputConnectors().indexOf(output);
        if (indexOfOutput == -1) {
            return false;
        }

        List<InputConnector> inputConnectors = getAllInputConnectors();
        List<Wire> inputs = new ArrayList<>();
        List<Wire> outputs = new ArrayList<>();
        List<Boolean> inputsDefValue = new ArrayList<>();

        board.getElementsInOrder().forEach(element -> {
            if (element instanceof Wire) {
                Wire wire = (Wire) element;
                if (wire.isInternalGateWire()) return;

                if (!wire.getInputConnector().hasConnected()) {
                    inputs.add(wire);
                    inputsDefValue.add(wire.isInputValue());
                }

                if (!wire.getOutputConnector().hasConnected()) {
                    outputs.add(wire);
                }
            }
        });

        if (inputs.size() != inputConnectors.size()) {
            return false;
        }

        if (outputs.size() <= indexOfOutput) {
            return false;
        }

        for (int i = 0; i < inputs.size(); i++) {
            inputs.get(i).setInputValue(inputConnectors.get(i).calculate());
        }
        boolean out = outputs.get(indexOfOutput).calculate(null);

        for (int i = 0; i < inputs.size(); i++) {
            inputs.get(i).setInputValue(inputsDefValue.get(i));
        }

        return out;
    }
}
