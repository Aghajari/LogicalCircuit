package com.aghajari.circuit.elements;

import com.aghajari.circuit.CircuitElement;
import com.aghajari.circuit.connector.InputConnector;
import com.aghajari.circuit.connector.OutputConnector;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.List;

public class Wire extends Pane implements CircuitElement {

    private final Line line = new Line();
    private final Circle inputCircle = new Circle();
    private final Circle outputCircle = new Circle();
    private final Circle centerCircle = new Circle();

    private final Label text = new Label();

    private final InputConnector inputConnector = new InputConnector();
    private final OutputConnector outputConnector = new OutputConnector();

    private boolean inputValue;
    private boolean gravityEnabled = true;
    private boolean isInternalGateWire = false;

    public Wire() {
        line.setStroke(WidgetStyle.WIRE_COLOR);
        line.setStrokeWidth(WidgetStyle.STROKE_SIZE);

        initCircle(inputCircle, WidgetStyle.CircleType.INPUT);
        initCircle(outputCircle, WidgetStyle.CircleType.OUTPUT);
        initCircle(centerCircle, WidgetStyle.CircleType.SELECTOR);
        setSelectorsVisible(false);

        WidgetStyle.apply(text);
        centerCircle.setOpacity(0.6);

        getChildren().addAll(line, inputCircle, outputCircle, text, centerCircle);
        line.setOnMouseClicked(event -> setSelectorsVisible(!centerCircle.isVisible()));
        text.setOnMouseClicked(line.getOnMouseClicked());

        inputConnector.setOwner(this, inputCircle);
        outputConnector.setOwner(this, outputCircle);

        text.widthProperty().addListener((observableValue, number, t1) -> layoutCircle());
        text.heightProperty().addListener((observableValue, number, t1) -> layoutCircle());
    }

    private void initCircle(Circle circle, WidgetStyle.CircleType type) {
        WidgetStyle.apply(circle, type);

        circle.setOnMouseDragged(event -> {
            if (!circle.isVisible()) return;

            if (circle == inputCircle) {
                setStartX(event.getSceneX());
                setStartY(event.getSceneY());
                if (gravityEnabled && !inputConnector.hasConnected()) {
                    getBoard().findConnectionForInput(this);
                }

            } else if (circle == outputCircle) {
                setEndX(event.getSceneX());
                setEndY(event.getSceneY());
                if (gravityEnabled && !outputConnector.hasConnected()) {
                    getBoard().findConnectionForOutput(this);
                }

            } else {
                double width = getEndX() - getStartX();
                double height = getEndY() - getStartY();

                double centerX = event.getSceneX();
                double centerY = event.getSceneY();

                setStartX(centerX - width / 2);
                setStartY(centerY - height / 2);
                setEndX(getStartX() + width);
                setEndY(getStartY() + height);

                if (gravityEnabled && !inputConnector.hasConnected()) {
                    getBoard().findConnectionForInput(this);
                }
                if (gravityEnabled && !outputConnector.hasConnected()) {
                    getBoard().findConnectionForOutput(this);
                }
            }

            outputConnector.requestUpdate();
            inputConnector.requestUpdate();
        });
    }

    public double getStartX() {
        return line.getStartX();
    }

    public void setStartX(double startX) {
        line.setStartX(startX);
        if (isInternalGateWire && !outputConnector.hasConnected())
            line.setEndX(startX);
        layoutCircle();
    }

    public double getStartY() {
        return line.getStartY();
    }

    public void setStartY(double startY) {
        line.setStartY(startY);
        if (isInternalGateWire && !outputConnector.hasConnected())
            line.setEndY(startY);
        layoutCircle();
    }

    public double getEndX() {
        return line.getEndX();
    }

    public void setEndX(double endX) {
        line.setEndX(endX);
        layoutCircle();
    }

    public double getEndY() {
        return line.getEndY();
    }

    public void setEndY(double endY) {
        line.setEndY(endY);
        layoutCircle();
    }

    private void layoutCircle() {
        inputCircle.setLayoutX(getStartX() - inputCircle.getRadius() - inputCircle.getStrokeWidth());
        inputCircle.setLayoutY(getStartY() - inputCircle.getRadius() / 2);
        if (isInternalGateWire) {
            // match inputCircle of wire with outputCircle of gate
            Bounds bounds = inputConnector.getBounds();
            Bounds targetBounds = inputConnector.getConnection().getBounds();
            inputCircle.setLayoutX(inputCircle.getLayoutX() + targetBounds.getMinX() - bounds.getMinX());
            inputCircle.setLayoutY(inputCircle.getLayoutY() + targetBounds.getMinY() - bounds.getMinY());
        }

        outputCircle.setLayoutX(getEndX() + outputCircle.getRadius() - outputCircle.getStrokeWidth());
        outputCircle.setLayoutY(getEndY() - outputCircle.getRadius() / 2);

        centerCircle.setLayoutX(line.getLayoutBounds().getCenterX() - centerCircle.getRadius() / 2);
        centerCircle.setLayoutY(line.getLayoutBounds().getCenterY() - centerCircle.getRadius() / 2);

        text.setLayoutX(line.getLayoutBounds().getCenterX() - text.getWidth() / 2);
        text.setLayoutY(line.getLayoutBounds().getCenterY() - text.getHeight() / 2);
    }

    public InputConnector getInputConnector() {
        return inputConnector;
    }

    public OutputConnector getOutputConnector() {
        return outputConnector;
    }

    void setInternalGateWire(OutputConnector outputConnector) {
        isInternalGateWire = true;
        line.setOnMouseClicked(null);
        text.setOnMouseClicked(null);
        inputCircle.setOnMouseClicked(null);
        inputCircle.setOnMouseDragged(null);
        inputCircle.setOpacity(0.0);
        centerCircle.setOpacity(0.0);
        inputConnector.connect(outputConnector);
        outputCircle.setVisible(true);
        layoutCircle();
        setSelectorsVisible(false);
    }

    public boolean isInternalGateWire() {
        return isInternalGateWire;
    }

    @Override
    public List<InputConnector> getAllInputConnectors() {
        return List.of(inputConnector);
    }

    @Override
    public List<OutputConnector> getAllOutputConnectors() {
        return List.of(outputConnector);
    }

    @Override
    public List<Node> getMovableNodes() {
        return List.of(centerCircle, inputCircle, outputCircle);
    }

    @Override
    public List<Node> getClickableNodes() {
        return List.of(line, text);
    }

    @Override
    public void setElementName(String name) {
        text.setText(name);
    }

    @Override
    public String getElementName() {
        return text.getText();
    }

    public boolean isInputValue() {
        return inputValue;
    }

    public void setInputValue(boolean inputValue) {
        this.inputValue = inputValue;
    }

    public boolean isGravityEnabled() {
        return gravityEnabled;
    }

    public void setGravityEnabled(boolean gravityEnabled) {
        this.gravityEnabled = gravityEnabled;
    }

    @Override
    public void setSelectorsVisible(boolean visible) {
        visible &= !isInternalGateWire;
        requestSelectors(visible);
        centerCircle.setVisible(visible);
    }

    @Override
    public boolean calculate(OutputConnector connector) {
        boolean out = inputValue;
        if (getInputConnector().hasConnected()) {
            out = getInputConnector().calculate();
        }
        WidgetStyle.apply(line, out);
        return out;
    }
}
