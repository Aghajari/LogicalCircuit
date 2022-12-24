package com.aghajari.circuit.elements;

import com.aghajari.circuit.connector.InputConnector;
import com.aghajari.circuit.connector.OutputConnector;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;

public class GateAND extends BaseGate {

    public GateAND() {
        MoveTo moveTo = new MoveTo(WidgetStyle.RADIUS, WidgetStyle.RADIUS);

        LineTo lineToBottom = new LineTo();
        lineToBottom.setX(moveTo.getX());
        lineToBottom.yProperty().bind(prefHeightProperty().subtract(WidgetStyle.RADIUS));

        LineTo lineToBottomRight = new LineTo();
        lineToBottomRight.yProperty().bind(lineToBottom.yProperty());
        lineToBottomRight.xProperty().bind(prefWidthProperty().subtract(WidgetStyle.RADIUS * 6));

        ArcTo arc = new ArcTo();
        arc.xProperty().bind(lineToBottomRight.xProperty());
        arc.setY(moveTo.getY());
        arc.radiusXProperty().bind(
                prefWidthProperty()
                        .subtract(lineToBottomRight.xProperty())
                        .subtract(WidgetStyle.RADIUS)
        );
        arc.radiusYProperty().bind(
                prefHeightProperty()
                        .subtract(WidgetStyle.RADIUS * 2)
                        .divide(2)
        );

        LineTo lineToLeftTop = new LineTo();
        lineToLeftTop.setX(moveTo.getX());
        lineToLeftTop.setY(moveTo.getY());

        path.getElements().addAll(moveTo, lineToBottom, lineToBottomRight, arc, lineToLeftTop);

        text.setLayoutX(moveTo.getX() + 10);
        text.setLayoutY(moveTo.getY() + 10);
        text.setMaxWidth(100 - text.getLayoutX() * 2);

        initialize();
    }

    @Override
    protected boolean isGateResizable() {
        return true;
    }

    @Override
    protected double getCirclePadding(boolean output) {
        return output ? WidgetStyle.RADIUS : WidgetStyle.RADIUS / 2;
    }

    @Override
    public boolean calculate(OutputConnector connector) {
        return getAllInputConnectors().stream()
                .filter(InputConnector::hasConnected)
                .allMatch(InputConnector::calculate);
    }
}
