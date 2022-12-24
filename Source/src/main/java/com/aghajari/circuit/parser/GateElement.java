package com.aghajari.circuit.parser;

import java.util.ArrayList;
import java.util.List;

public class GateElement extends Element {

    private final List<WireElement> internalWires = new ArrayList<>();
    private double layoutX;
    private double layoutY;
    private double prefWidth;
    private double prefHeight;

    public List<WireElement> getInternalWires() {
        return internalWires;
    }

    public double getLayoutX() {
        return layoutX;
    }

    public void setLayoutX(double layoutX) {
        this.layoutX = layoutX;
    }

    public double getLayoutY() {
        return layoutY;
    }

    public void setLayoutY(double layoutY) {
        this.layoutY = layoutY;
    }

    public double getPrefWidth() {
        return prefWidth;
    }

    public void setPrefWidth(double prefWidth) {
        this.prefWidth = prefWidth;
    }

    public double getPrefHeight() {
        return prefHeight;
    }

    public void setPrefHeight(double prefHeight) {
        this.prefHeight = prefHeight;
    }
}
