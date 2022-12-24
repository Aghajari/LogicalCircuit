package com.aghajari.circuit.parser;

public class NumberGateElement extends GateElement {

    private int bitCount;
    private int number;

    public int getBitCount() {
        return bitCount;
    }

    public void setBitCount(int bitCount) {
        this.bitCount = bitCount;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
