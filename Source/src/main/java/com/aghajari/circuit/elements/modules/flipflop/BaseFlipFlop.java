package com.aghajari.circuit.elements.modules.flipflop;

import com.aghajari.circuit.connector.InputConnector;
import com.aghajari.circuit.connector.OutputConnector;
import com.aghajari.circuit.elements.BaseGate;
import com.aghajari.circuit.elements.WidgetStyle;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.List;

public abstract class BaseFlipFlop extends BaseGate {

    private String elementName;
    private boolean prevEnabled;
    private boolean prevState;

    private final Circle notCircle = new Circle();
    private final Path flipFlopPath = new Path();

    public BaseFlipFlop() {
        flipFlopPath.setStroke(WidgetStyle.GATE_COLOR);
        flipFlopPath.setStrokeWidth(WidgetStyle.STROKE_SIZE);
        getChildren().add(flipFlopPath);

        if (isNegative()) {
            WidgetStyle.apply(notCircle, WidgetStyle.CircleType.NOT);
            getChildren().add(notCircle);
        }

        drawRectAsPath();
        initialize();
        setPrefWidth(120);
        text.setStyle("-fx-background-color: transparent;");
        requestUpdate();
    }

    @Override
    public void requestUpdate() {
        super.requestUpdate();

        Circle in = inputCircles.get(1);

        MoveTo moveTo = new MoveTo(
                in.getLayoutX() + in.getRadius() / 2,
                in.getLayoutY() - in.getRadius() - flipFlopPath.getStrokeWidth()
        );
        LineTo topLine = new LineTo(
                in.getLayoutX() + in.getRadius() * 3,
                moveTo.getY() + in.getRadius() * 2
        );
        LineTo bottomLine = new LineTo(
                moveTo.getX(),
                topLine.getY() + in.getRadius() * 2
        );
        flipFlopPath.getElements().clear();
        flipFlopPath.getElements().addAll(moveTo, topLine, bottomLine);

        if (isNegative()) {
            notCircle.setLayoutY(in.getLayoutY());
            notCircle.setLayoutX(in.getLayoutX() -
                    notCircle.getRadius() -
                    notCircle.getStrokeWidth()
            );
            in.setLayoutX(notCircle.getLayoutX() -
                    notCircle.getRadius() * 2 -
                    notCircle.getStrokeWidth()
            );
        }
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
        text.setText(elementName + "\n(" + getModuleName() + ")");
    }

    protected String getModuleName() {
        return "FlipFlop";
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

        boolean out = calculate(
                prevEnabled,
                enabled,
                inputs.get(0).calculate(),
                prevState
        );
        prevState = out;
        prevEnabled = enabled;

        return (index == 0) == out;
    }

    protected boolean isNegative() {
        return false;
    }

    protected abstract boolean calculate(
            boolean oldEnabled,
            boolean newEnabled,
            boolean d,
            boolean prevState
    );
}
