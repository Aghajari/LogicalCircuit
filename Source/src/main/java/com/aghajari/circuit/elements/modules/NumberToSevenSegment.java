package com.aghajari.circuit.elements.modules;

import com.aghajari.circuit.connector.InputConnector;
import com.aghajari.circuit.connector.OutputConnector;
import com.aghajari.circuit.elements.BaseGate;

import java.util.Arrays;
import java.util.List;

public class NumberToSevenSegment extends BaseGate {

    private final boolean[] array = new boolean[7];
    private String elementName;
    private int prevNumber = -2;

    public NumberToSevenSegment() {
        drawRectAsPath();
        initialize();
        setPrefHeight((getOutputCount() + 1) * 30);

        text.widthProperty().addListener((observableValue, number, t1) -> {
            setPrefWidth(text.getWidth() + centerCircle.getRadius() * 6);
            requestUpdate();
        });
    }

    @Override
    protected int getInputCount(int count) {
        return 4;
    }

    @Override
    protected int getOutputCount() {
        return 7;
    }

    @Override
    protected boolean isTextOnCenter() {
        return true;
    }

    private void initModuleText() {
        text.setText(elementName + "\n(NT7)");
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
        if (index >= array.length || index < 0) return false;

        int number = 0;
        boolean hasConnected = false;
        List<InputConnector> inputs = getAllInputConnectors();
        for (int i = 0; i < inputs.size(); i++) {
            hasConnected |= inputs.get(i).hasConnected();

            if (inputs.get(i).calculate()) {
                hasConnected = true;
                number |= (1 << i);
            }
        }

        if (!hasConnected) number = -1;
        if (prevNumber == number) return array[index];
        prevNumber = number;

        switch (number) {
            case 0:
                Arrays.fill(array, true);
                array[6] = false;
                break;
            case 1:
                Arrays.fill(array, false);
                array[1] = array[2] = true;
                break;
            case 2:
                Arrays.fill(array, true);
                array[2] = array[5] = false;
                break;
            case 3:
                Arrays.fill(array, true);
                array[4] = array[5] = false;
                break;
            case 4:
                Arrays.fill(array, true);
                array[0] = array[3] = array[4] = false;
                break;
            case 5:
                Arrays.fill(array, true);
                array[1] = array[4] = false;
                break;
            case 6:
                Arrays.fill(array, true);
                array[1] = false;
                break;
            case 7:
                Arrays.fill(array, false);
                array[0] = array[1] = array[2] = true;
                break;
            case 8:
                Arrays.fill(array, true);
                break;
            case 9:
                Arrays.fill(array, true);
                array[4] = false;
                break;
            default:
                Arrays.fill(array, false);
                array[6] = true;
                break;
        }

        return array[index];
    }
}
