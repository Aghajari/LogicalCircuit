package com.aghajari.circuit.elements;

import com.aghajari.circuit.Gate;
import com.aghajari.circuit.connector.InputConnector;
import com.aghajari.circuit.connector.OutputConnector;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseGate extends Pane implements Gate {

    protected final Path path = new Path();
    protected final Label text = new Label();

    protected final Circle topCircle = new Circle();
    protected final Circle bottomCircle = new Circle();
    protected final Circle centerCircle = new Circle();

    protected final List<Circle> outputCircles = new ArrayList<>();
    protected final List<Circle> outputNotCircles = new ArrayList<>();
    protected final List<OutputConnector> outputConnectors = new ArrayList<>();

    protected final List<Circle> inputCircles = new ArrayList<>();
    protected final List<InputConnector> inputConnectors = new ArrayList<>();

    protected final List<Wire> internalWires = new ArrayList<>();

    private boolean gravityEnabled = true;

    protected void initialize() {
        setPrefWidth(100);
        setPrefHeight(100);
        WidgetStyle.apply(text);

        path.setStrokeWidth(WidgetStyle.STROKE_SIZE);
        path.setStroke(WidgetStyle.GATE_COLOR);
        path.setFill(Color.TRANSPARENT);

        initCircle(topCircle);
        initCircle(bottomCircle);
        initCircle(centerCircle);

        setSelectorsVisible(false);
        path.setOnMouseClicked(event -> setSelectorsVisible(!centerCircle.isVisible()));

        getChildren().addAll(text, path, topCircle, bottomCircle, centerCircle);

        if (isTextOnCenter()) {
            centerCircle.setOpacity(0.5);

            ChangeListener<Number> listener = (observableValue, number, t1) -> layoutText();
            text.widthProperty().addListener(listener);
            text.heightProperty().addListener(listener);
        }

        requestUpdate();
    }

    protected void drawRectAsPath() {
        MoveTo moveTo = new MoveTo(WidgetStyle.RADIUS, WidgetStyle.RADIUS);

        LineTo lineToBottom = new LineTo();
        lineToBottom.setX(moveTo.getX());
        lineToBottom.yProperty().bind(prefHeightProperty().subtract(WidgetStyle.RADIUS));

        LineTo lineToBottomRight = new LineTo();
        lineToBottomRight.yProperty().bind(lineToBottom.yProperty());
        lineToBottomRight.xProperty().bind(prefWidthProperty().subtract(WidgetStyle.RADIUS));

        LineTo lineToTop = new LineTo();
        lineToTop.yProperty().bind(moveTo.yProperty());
        lineToTop.xProperty().bind(lineToBottomRight.xProperty());

        LineTo lineToLeftTop = new LineTo();
        lineToLeftTop.setX(moveTo.getX());
        lineToLeftTop.setY(moveTo.getY());

        path.getElements().addAll(moveTo, lineToBottom, lineToBottomRight, lineToTop, lineToLeftTop);
    }

    protected void initCircle(Circle circle) {
        WidgetStyle.apply(circle, WidgetStyle.CircleType.SELECTOR);

        circle.setOnMouseDragged(event -> {
            if (!circle.isVisible()) return;

            if (circle == topCircle) {
                double newTop = event.getSceneY();
                double newBottom = Math.max(newTop + 100, getLayoutY() + getPrefHeight());
                setLayoutY(newTop);
                setPrefHeight(newBottom - newTop);

            } else if (circle == bottomCircle) {
                double newBottom = event.getSceneY();
                double newTop = Math.min(newBottom - 100, getLayoutY());
                setLayoutY(newTop);
                setPrefHeight(newBottom - newTop);

            } else if (circle == centerCircle) {
                setLayoutX(event.getSceneX() - centerCircle.getLayoutX());
                setLayoutY(event.getSceneY() - centerCircle.getLayoutY());

                if (gravityEnabled) {
                    getBoard().findConnectionForInput(this);
                    getBoard().findConnectionForOutput(this);
                }
            }

            requestUpdate();
        });
    }

    public void requestUpdate() {
        Bounds pathBounds = path.getLayoutBounds();

        topCircle.setLayoutX(pathBounds.getWidth() / 2 - topCircle.getRadius());
        topCircle.setLayoutY(pathBounds.getMinY() + topCircle.getRadius() / 2 - getCirclePadding(false));
        bottomCircle.setLayoutX(topCircle.getLayoutX());
        bottomCircle.setLayoutY(pathBounds.getMaxY() - bottomCircle.getRadius() / 2 - getCirclePadding(false));
        centerCircle.setLayoutX(topCircle.getLayoutX());
        centerCircle.setLayoutY(getPrefHeight() / 2 - centerCircle.getRadius() / 2);
        if (isTextOnCenter()) {
            bottomCircle.setLayoutX(bottomCircle.getLayoutX() + bottomCircle.getRadius());
            topCircle.setLayoutX(topCircle.getLayoutX() + topCircle.getRadius());
            centerCircle.setLayoutX(centerCircle.getLayoutX() + centerCircle.getRadius());
        }

        addOutputCircles();
        addInputCircles();
        layoutText();
    }

    protected void layoutText() {
        if (isTextOnCenter()) {
            text.setLayoutX(path.getLayoutBounds().getCenterX() - text.getWidth() / 2);
            text.setLayoutY(path.getLayoutBounds().getCenterY() - text.getHeight() / 2);
        }
    }

    protected double getCirclePadding(boolean output) {
        return 0;
    }

    protected void addInputCircles() {
        inputCircles.forEach(getChildren()::remove);
        inputCircles.clear();

        int count = getInputCount((int) (getPrefHeight() / 50));
        double space = (getPrefHeight() - count * WidgetStyle.BIG_RADIUS) / (count + 1);
        double y = space;

        if (count > inputConnectors.size()) {
            while (inputConnectors.size() != count) {
                inputConnectors.add(new InputConnector());
            }
        } else if (inputConnectors.size() > count) {
            while (inputConnectors.size() != count) {
                inputConnectors.remove(inputConnectors.size() - 1).disconnect();
            }
        }

        for (int i = 0; i < count; i++) {
            Circle input = new Circle();
            WidgetStyle.apply(input, WidgetStyle.CircleType.GATE_INPUT);

            input.setLayoutX(WidgetStyle.RADIUS - input.getRadius() / 2);
            input.setLayoutY(y);
            initInputCircle(input);

            y += space + input.getRadius();
            getChildren().add(input);
            inputCircles.add(input);

            inputConnectors.get(i).setOwner(this, input);
            inputConnectors.get(i).requestUpdate();
        }
    }

    protected void addOutputCircles() {
        Bounds pathBounds = path.getLayoutBounds();
        int count = getOutputCount();

        if (count > outputCircles.size()) {
            while (outputCircles.size() != count) {
                Circle outputCircle = new Circle();
                WidgetStyle.apply(outputCircle, WidgetStyle.CircleType.GATE_OUTPUT);

                outputCircles.add(outputCircle);
                OutputConnector c = new OutputConnector();
                c.setOwner(this, outputCircle);
                outputConnectors.add(c);
                getChildren().add(outputCircle);

                if (isNotGate()) {
                    Circle notCircle = new Circle();
                    WidgetStyle.apply(notCircle, WidgetStyle.CircleType.NOT);

                    getChildren().add(0, notCircle);
                    notCircle.setOnMouseClicked(path.getOnMouseClicked());
                    outputNotCircles.add(notCircle);
                }
            }
        } else if (outputCircles.size() > count) {
            while (outputCircles.size() != count) {
                int index = outputCircles.size() - 1;
                getChildren().remove(outputCircles.remove(index));
                outputConnectors.remove(index).disconnect();

                if (isNotGate()) {
                    outputNotCircles.remove(index);
                }
            }
        }

        double space = (getPrefHeight() - count * WidgetStyle.BIG_RADIUS) / (count + 1);
        double y = space;

        for (int i = 0; i < count; i++) {
            Circle output = outputCircles.get(i);
            output.setLayoutX(pathBounds.getWidth() - output.getRadius() + getCirclePadding(true));
            output.setLayoutY(y);
            initOutputCircle(output);

            if (isNotGate()) {
                Circle notCircle = outputNotCircles.get(i);
                double xPadding = output.getStrokeWidth() * 2;
                notCircle.setLayoutX(output.getLayoutX() + xPadding);
                notCircle.setLayoutY(output.getLayoutY());
                if (count > 1)
                    xPadding -= output.getRadius();

                output.setLayoutX(output.getLayoutX() +
                        output.getRadius() * 2 +
                        output.getStrokeWidth() +
                        xPadding
                );
            }

            y += space + output.getRadius();
            outputConnectors.get(i).requestUpdate();
        }

        if (count > 1) {
            if (count > internalWires.size()) {
                while (internalWires.size() != count) {
                    int index = internalWires.size();
                    Circle outputCircle = outputCircles.get(index);
                    OutputConnector output = outputConnectors.get(index);
                    outputCircle.setOpacity(0.0);

                    final InputConnector targetConnection;
                    if (output.hasConnected()) {
                        targetConnection = output.getConnection();
                        output.disconnect();
                    } else {
                        targetConnection = null;
                    }

                    Wire wire = new Wire();
                    wire.setElementName("");
                    wire.setInternalGateWire(output);
                    internalWires.add(wire);

                    if (getBoard() != null) {
                        addInternalWireToParent(wire, output, outputCircle, targetConnection);
                        continue;
                    }

                    parentProperty().addListener(new ChangeListener<>() {
                        @Override
                        public void changed(ObservableValue<? extends Parent> observableValue, Parent parent, Parent t1) {
                            if (getBoard() != null) {
                                parentProperty().removeListener(this);
                                if (internalWires.contains(wire)) {
                                    addInternalWireToParent(wire, output, outputCircle, targetConnection);
                                }
                            }
                        }
                    });
                }
            } else if (count < internalWires.size()) {
                while (internalWires.size() != count) {
                    Wire wire = internalWires.remove(internalWires.size() - 1);
                    wire.remove();
                    if (wire.getBoard() != null) {
                        wire.getBoard().getChildren().remove(wire);
                    }
                }
            }
        } else if (count == 1) {
            outputCircles.get(0).setOpacity(1);
            if (internalWires.size() >= 1) {
                Wire wire = internalWires.get(0);
                if (wire.getOutputConnector().hasConnected()) {
                    InputConnector connection = wire.getOutputConnector().getConnection();
                    wire.disconnectOutputConnectors();
                    outputConnectors.get(0).disconnect();
                    outputConnectors.get(0).connect(connection);
                }
            }

            while (!internalWires.isEmpty()) {
                Wire wire = internalWires.remove(internalWires.size() - 1);
                wire.remove();
                if (wire.getBoard() != null) {
                    wire.getBoard().getChildren().remove(wire);
                }
            }
        }
    }

    private void addInternalWireToParent(
            Wire wire,
            OutputConnector output,
            Circle circle,
            InputConnector targetConnection
    ) {
        if (wire.getParent() == null) {
            getBoard().addElement(wire);
            wire.setSelectorsVisible(false);

            Bounds bounds = output.getBounds();
            wire.setStartX(bounds.getCenterX());
            wire.setStartY(bounds.getCenterY());
            initInternalWire(wire, output, circle);
        }

        if (targetConnection != null && !wire.getOutputConnector().hasConnected()) {
            wire.getOutputConnector().connect(targetConnection);
        }
    }

    protected void initInternalWire(Wire wire, OutputConnector output, Circle circle) {
    }

    protected void initInputCircle(Circle input) {
    }

    protected void initOutputCircle(Circle output) {
        if (isTextOnCenter()) {
            output.setLayoutX(output.getLayoutX() + output.getRadius());
        }
    }

    protected int getInputCount(int count) {
        return count;
    }

    protected int getOutputCount() {
        return 1;
    }

    protected boolean isTextOnCenter() {
        return false;
    }

    protected boolean isNotGate() {
        return false;
    }

    protected boolean isGateResizable() {
        return false;
    }

    public boolean isPrefSizeFixed() {
        return false;
    }

    public List<Wire> getInternalWires() {
        return internalWires;
    }

    public boolean isGravityEnabled() {
        return gravityEnabled;
    }

    public void setGravityEnabled(boolean gravityEnabled) {
        this.gravityEnabled = gravityEnabled;
        internalWires.forEach(wire -> wire.setGravityEnabled(gravityEnabled));
    }

    @Override
    public List<InputConnector> getAllInputConnectors() {
        return inputConnectors;
    }

    @Override
    public List<OutputConnector> getAllOutputConnectors() {
        return outputConnectors;
    }

    public int getRealNumberOfEmptyOutputConnectors() {
        if (internalWires.isEmpty())
            return getAllEmptyOutputConnectors().size();
        else {
            return (int) internalWires.stream()
                    .filter(wire -> !wire.getOutputConnector().hasConnected())
                    .count();
        }
    }

    @Override
    public List<Node> getMovableNodes() {
        if (centerCircle.isVisible()) {
            return List.of(centerCircle, topCircle, bottomCircle);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void setSelectorsVisible(boolean visible) {
        requestSelectors(visible);
        topCircle.setVisible(visible && isGateResizable());
        bottomCircle.setVisible(visible && isGateResizable());
        centerCircle.setVisible(visible);
    }

    @Override
    public List<Node> getClickableNodes() {
        return List.of(path);
    }

    @Override
    public void setElementName(String name) {
        text.setText(name);
    }

    @Override
    public String getElementName() {
        return text.getText();
    }

    @Override
    public void disconnectOutputConnectors() {
        if (internalWires.isEmpty()) {
            getAllOutputConnectors().forEach(OutputConnector::disconnect);
        } else {
            internalWires.forEach(wire -> wire.getOutputConnector().disconnect());
        }
    }

    @Override
    public void remove() {
        disconnectAllConnectors();

        internalWires.forEach(wire -> {
            wire.remove();
            getBoard().getChildren().remove(wire);
        });
    }
}
