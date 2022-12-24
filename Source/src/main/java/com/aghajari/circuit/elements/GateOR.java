package com.aghajari.circuit.elements;

import com.aghajari.circuit.connector.InputConnector;
import com.aghajari.circuit.connector.OutputConnector;
import javafx.geometry.Bounds;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.Circle;
import javafx.scene.shape.MoveTo;

public class GateOR extends BaseGate {

    public GateOR() {
        MoveTo moveTo = new MoveTo(WidgetStyle.RADIUS, WidgetStyle.RADIUS);

        ArcTo arcToBottom = new ArcTo();
        arcToBottom.setX(moveTo.getX());
        arcToBottom.yProperty().bind(prefHeightProperty().subtract(moveTo.getY()));
        arcToBottom.setRadiusX(20);
        arcToBottom.setSweepFlag(true);
        arcToBottom.radiusYProperty().bind(
                prefHeightProperty()
                        .subtract(WidgetStyle.RADIUS * 2)
                        .divide(2)
        );

        ArcTo arcToCenter = new ArcTo();
        arcToCenter.xProperty().bind(prefWidthProperty().subtract(moveTo.getX()));
        arcToCenter.yProperty().bind(prefHeightProperty().divide(2));
        arcToCenter.setRadiusX(50);
        arcToCenter.radiusYProperty().bind(
                prefHeightProperty()
                        .subtract(WidgetStyle.RADIUS * 2)
                        .divide(5)
        );

        ArcTo arcToFirst = new ArcTo();
        arcToCenter.xProperty().bind(moveTo.xProperty());
        arcToCenter.yProperty().bind(moveTo.yProperty());
        arcToCenter.setRadiusX(50);
        arcToCenter.radiusYProperty().bind(
                prefHeightProperty()
                        .subtract(WidgetStyle.RADIUS * 2)
                        .divide(5)
        );
        path.getElements().addAll(moveTo, arcToBottom, arcToCenter, arcToFirst);

        text.setLayoutX(moveTo.getX() + 30);
        text.setLayoutY(moveTo.getY() + 10);
        text.setMaxWidth(100);

        initialize();
    }

    @Override
    protected boolean isGateResizable() {
        return true;
    }

    @Override
    protected int getInputCount(int count) {
        return (count % 2 == 1 && count > 1) ? count - 1 : count;
    }

    protected double getOvalRadiusX(){
        return 50;
    }

    @Override
    protected void initInputCircle(Circle input) {
        super.initInputCircle(input);
        double y = input.getLayoutY();

        Bounds pathBounds = path.getLayoutBounds();
        double R = pathBounds.getHeight() / 2;
        double r = getOvalRadiusX();

        double y2 = y;
        if (y2 > R) {
            y2 = 2 * R - y;
        }
        double L = (r / R) * Math.sqrt(R * R - y2 * y2);
        input.setLayoutX(pathBounds.getMinX() - L + r + 10);
    }

    @Override
    public boolean calculate(OutputConnector connector) {
        return getAllInputConnectors().stream()
                .filter(InputConnector::hasConnected)
                .anyMatch(InputConnector::calculate);
    }
}
