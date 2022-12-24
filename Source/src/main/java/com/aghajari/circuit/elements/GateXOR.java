package com.aghajari.circuit.elements;

import com.aghajari.circuit.connector.InputConnector;
import com.aghajari.circuit.connector.OutputConnector;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.Circle;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class GateXOR extends GateOR {

    public GateXOR() {
        super();

        MoveTo moveTo = (MoveTo) path.getElements().get(0);
        ArcTo sampleArc = (ArcTo) path.getElements().get(1);

        ArcTo arcTo = new ArcTo();
        arcTo.setX(0);
        arcTo.yProperty().bind(sampleArc.yProperty());
        arcTo.radiusXProperty().bind(sampleArc.radiusXProperty().subtract(moveTo.xProperty()));
        arcTo.radiusYProperty().bind(sampleArc.radiusYProperty());
        arcTo.setSweepFlag(sampleArc.isSweepFlag());
        arcTo.setLargeArcFlag(sampleArc.isLargeArcFlag());
        arcTo.setXAxisRotation(sampleArc.getXAxisRotation());

        Path path2 = new Path();
        path2.setStroke(path.getStroke());
        path2.setStrokeWidth(path.getStrokeWidth());

        path2.getElements().addAll(new MoveTo(0, moveTo.getY()), arcTo);
        getChildren().add(0, path2);
    }

    @Override
    protected double getOvalRadiusX() {
        MoveTo moveTo = (MoveTo) path.getElements().get(0);
        return super.getOvalRadiusX() - moveTo.getX();
    }

    @Override
    protected void initInputCircle(Circle input) {
        super.initInputCircle(input);
        input.setLayoutX(input.getLayoutX() - 10);
    }

    @Override
    public boolean calculate(OutputConnector connector) {
        return getAllInputConnectors().stream()
                .filter(InputConnector::hasConnected)
                .map(InputConnector::calculate)
                .reduce((a, b) -> a ^ b).orElse(false);
    }
}
