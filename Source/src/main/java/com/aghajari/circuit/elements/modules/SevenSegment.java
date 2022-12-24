package com.aghajari.circuit.elements.modules;

import com.aghajari.circuit.connector.InputConnector;
import com.aghajari.circuit.connector.OutputConnector;
import com.aghajari.circuit.elements.BaseGate;
import com.aghajari.circuit.elements.WidgetStyle;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import java.util.List;

public class SevenSegment extends BaseGate {

    private static final char[] TEXT = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G'
    };

    private static final int TRIANGLE = 10;
    private static final int SIZE = 50 + TRIANGLE * 2;

    private final Circle dpCircle = new Circle();
    private final Path[] paths = new Path[7];
    private final Label[] texts = new Label[8];
    private String elementName;

    public SevenSegment() {
        double w = 220;
        double h = getInputCount(0) * 30 + 25;

        for (int i = 0; i < 7; i++) {
            paths[i] = new Path();
            updateShape(paths[i], false);

            texts[i] = new Label();
            texts[i].setText(String.valueOf(TEXT[i]));
            WidgetStyle.apply(texts[i]);
        }

        texts[7] = new Label();
        texts[7].setText("DP");
        WidgetStyle.apply(texts[7]);

        dpCircle.setRadius(TRIANGLE);
        dpCircle.setCenterX(dpCircle.getRadius() / 2);
        dpCircle.setCenterY(dpCircle.getRadius() / 2);
        dpCircle.setLayoutX(w - 40);
        dpCircle.setLayoutY(h - 45);
        texts[7].setLayoutX(dpCircle.getLayoutX() - 4);
        texts[7].setLayoutY(dpCircle.getLayoutY() + dpCircle.getRadius() * 2 - 4);
        texts[7].setStyle("-fx-background-color: transparent;");
        updateShape(dpCircle, false);

        double w2 = SIZE + TRIANGLE;
        double space = 4;
        double h2 = 2 * w2 + space * 4;
        double y = (h - h2) / 2;
        double x = ((w - 20) - w2) / 2;
        double x2 = x - space;
        double y2 = y + space;

        bindPath(x, y, true, paths[0]);
        bindPath(x, y + w2 + space * 2, true, paths[6]);
        bindPath(x, y + w2 * 2 + space * 4, true, paths[3]);
        bindPath(x += SIZE + TRIANGLE + space, y += space, false, paths[1]);
        bindPath(x, y + 2 * space + w2, false, paths[2]);
        bindPath(x2, y2, false, paths[5]);
        bindPath(x2, y2 + 2 * space + w2, false, paths[4]);

        getChildren().add(dpCircle);
        getChildren().addAll(paths);
        getChildren().addAll(texts);

        drawRectAsPath();
        initialize();
        setPrefWidth(w);
        setPrefHeight(h);
        requestUpdate();

        centerCircle.setOpacity(1.0);
        for (int i = 0; i < 7; i++) {
            int finalI = i;
            texts[i].widthProperty().addListener((o, a, b) -> bindText(finalI));
            texts[i].heightProperty().addListener((o, a, b) -> bindText(finalI));
            bindText(i);

            paths[i].setOnMouseClicked(path.getOnMouseClicked());
        }
        dpCircle.setOnMouseClicked(path.getOnMouseClicked());
    }

    private void bindPath(double firstX, double firstY, boolean horizontal, Path path) {
        MoveTo moveTo = new MoveTo(firstX, firstY);
        path.getElements().add(moveTo);

        if (horizontal) {
            LineTo l1 = new LineTo(firstX + TRIANGLE, firstY - TRIANGLE);
            LineTo l2 = new LineTo(l1.getX() + SIZE - TRIANGLE, l1.getY());
            LineTo l3 = new LineTo(l2.getX() + TRIANGLE, moveTo.getY());
            LineTo l4 = new LineTo(l2.getX(), moveTo.getY() + TRIANGLE);
            LineTo l5 = new LineTo(l1.getX(), l4.getY());
            LineTo l6 = new LineTo(moveTo.getX(), moveTo.getY());
            path.getElements().addAll(l1, l2, l3, l4, l5, l6);
        } else {
            LineTo l1 = new LineTo(firstX - TRIANGLE, firstY + TRIANGLE);
            LineTo l2 = new LineTo(l1.getX(), l1.getY() + SIZE - TRIANGLE);
            LineTo l3 = new LineTo(moveTo.getX(), l2.getY() + TRIANGLE);
            LineTo l4 = new LineTo(moveTo.getX() + TRIANGLE, l2.getY());
            LineTo l5 = new LineTo(l4.getX(), l1.getY());
            LineTo l6 = new LineTo(moveTo.getX(), moveTo.getY());
            path.getElements().addAll(l1, l2, l3, l4, l5, l6);
        }
    }

    private void bindText(int i) {
        Label text = texts[i];
        double textWidth = text.getWidth();
        double textHeight = text.getHeight();
        double space = 4;

        boolean left = (i == 4) || (i == 5);
        boolean right = (i == 1) || (i == 2);
        boolean top = (i == 0) || (i == 6);
        boolean bottom = (i == 3);

        Bounds bounds = paths[i].getLayoutBounds();
        if (left || right) {
            text.setLayoutY(bounds.getCenterY() - textHeight / 2);

            if (left)
                text.setLayoutX(bounds.getMinX() - space - textWidth);
            else
                text.setLayoutX(bounds.getMaxX() + space);
        }

        if (bottom || top) {
            text.setLayoutX(bounds.getCenterX() - textWidth / 2);

            if (top)
                text.setLayoutY(bounds.getMinY() - textHeight);
            else
                text.setLayoutY(bounds.getMaxY());
        }
    }

    @Override
    protected int getInputCount(int count) {
        return 8;
    }

    @Override
    protected int getOutputCount() {
        return 0;
    }

    @Override
    protected boolean isTextOnCenter() {
        return true;
    }

    @Override
    public void setElementName(String name) {
        elementName = name;
    }

    @Override
    public String getElementName() {
        return elementName;
    }

    @Override
    public boolean calculate(OutputConnector output) {
        List<InputConnector> inputs = getAllInputConnectors();
        for (int i = 0; i < inputs.size() - 1; i++) {
            updateShape(paths[i], inputs.get(i).calculate());
        }
        updateShape(dpCircle, inputs.get(inputs.size() - 1).calculate());

        return false;
    }

    private void updateShape(Shape shape, boolean connected) {
        shape.setStrokeWidth(WidgetStyle.STROKE_SIZE);
        if (connected) {
            shape.setStroke(WidgetStyle.WIRE_RUN_COLOR);
            shape.setFill(WidgetStyle.WIRE_RUN_COLOR);
            shape.setOpacity(1);
        } else {
            shape.setFill(Color.TRANSPARENT);
            shape.setStroke(WidgetStyle.WIRE_COLOR);
            shape.setOpacity(0.3);
        }
    }

    @Override
    public boolean isPrefSizeFixed() {
        return true;
    }
}
