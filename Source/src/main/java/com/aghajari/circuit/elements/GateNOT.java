package com.aghajari.circuit.elements;

import com.aghajari.circuit.connector.OutputConnector;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;

public class GateNOT extends BaseGate {

    public GateNOT() {
        super();

        MoveTo moveTo = new MoveTo(0, 0);

        LineTo lineToBottom = new LineTo();
        lineToBottom.setX(moveTo.getX());
        lineToBottom.yProperty().bind(prefHeightProperty());

        LineTo lineToCenter = new LineTo();
        lineToCenter.yProperty().bind(prefHeightProperty().divide(2));
        lineToCenter.xProperty().bind(prefWidthProperty().subtract(WidgetStyle.RADIUS * 4));

        LineTo lineToLeftTop = new LineTo();
        lineToLeftTop.setX(moveTo.getX());
        lineToLeftTop.setY(moveTo.getY());

        path.getElements().addAll(moveTo, lineToBottom, lineToCenter, lineToLeftTop);

        text.setOpacity(0);
        text.setLayoutX(20);
        text.setMaxWidth(40);

        initialize();
        setPrefHeight(50);
        setPrefWidth(60);

        requestUpdate();
    }

    @Override
    protected boolean isNotGate() {
        return true;
    }

    @Override
    protected int getInputCount(int count) {
        return 1;
    }

    @Override
    protected void initInputCircle(Circle input) {
        super.initInputCircle(input);
        input.setLayoutX(input.getLayoutX() - input.getRadius());
    }

    @Override
    public boolean calculate(OutputConnector connector) {
        return !getAllInputConnectors().get(0).calculate();
    }
}
