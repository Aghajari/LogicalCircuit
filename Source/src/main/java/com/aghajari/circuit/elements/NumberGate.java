package com.aghajari.circuit.elements;

import com.aghajari.circuit.connector.OutputConnector;

public class NumberGate extends BaseGate {

    private String elementName;

    private int number;
    private int bitCount = 1;

    public NumberGate() {
        drawRectAsPath();
        initialize();
        setPrefHeight(60);

        text.widthProperty().addListener((observableValue, number, t1) -> {
            setPrefWidth(text.getWidth() + centerCircle.getRadius() * 6);
            requestUpdate();
        });
    }

    @Override
    protected int getInputCount(int count) {
        return 0;
    }

    @Override
    protected int getOutputCount() {
        return bitCount;
    }

    @Override
    protected boolean isTextOnCenter() {
        return true;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
        initModuleText();
    }

    public int getBitCount() {
        return bitCount;
    }

    public void setBitCount(int bitCount) {
        if (this.bitCount == bitCount)
            return;

        this.bitCount = Math.max(Math.min(Integer.SIZE, bitCount), 1);
        setPrefHeight((bitCount + 1) * 30);
        requestUpdate();
    }

    private void initModuleText() {
        text.setText(elementName + "\n(" + number + ")");
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
        return (number & (1 << index)) != 0;
    }
}
