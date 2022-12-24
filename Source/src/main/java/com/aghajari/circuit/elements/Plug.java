package com.aghajari.circuit.elements;

import com.aghajari.circuit.connector.OutputConnector;
import javafx.scene.shape.*;

public class Plug extends BaseGate {

    public Plug() {
        super();

        MoveTo moveTo = new MoveTo(0, 0);
        moveTo.yProperty().bind(prefHeightProperty().divide(2));

        LineTo top = new LineTo(0, 0);
        LineTo right = new LineTo(0, 0);
        right.xProperty().bind(prefWidthProperty());

        MoveTo moveTo2 = new MoveTo(0, 0);
        moveTo2.yProperty().bind(prefHeightProperty().divide(2));

        LineTo bottom = new LineTo(0, 0);
        bottom.yProperty().bind(prefHeightProperty());
        LineTo right2 = new LineTo(0, 0);
        right2.yProperty().bind(prefHeightProperty());
        right2.xProperty().bind(prefWidthProperty());

        path.setStrokeLineJoin(StrokeLineJoin.ROUND);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.getElements().addAll(moveTo, top, right, moveTo2, bottom, right2);

        text.setOpacity(0);
        text.setLayoutX(20);
        text.setMaxWidth(40);

        initialize();
        setPrefHeight(50);
        setPrefWidth(50);
        requestUpdate();
    }

    @Override
    protected int getInputCount(int count) {
        return 1;
    }

    @Override
    protected int getOutputCount() {
        return 2;
    }

    @Override
    protected void initInputCircle(Circle input) {
        super.initInputCircle(input);
        input.setLayoutX(input.getLayoutX() - input.getRadius());
    }

    @Override
    protected void initOutputCircle(Circle output) {
        super.initOutputCircle(output);
        if (output == outputCircles.get(0)) {
            output.setLayoutY(-output.getStrokeWidth());
        } else {
            output.setLayoutY(getPrefHeight() - output.getStrokeWidth());
        }
        output.setLayoutX(getPrefWidth() - output.getRadius() + WidgetStyle.STROKE_SIZE);
    }

    @Override
    public boolean calculate(OutputConnector connector) {
        boolean out = getAllInputConnectors().get(0).calculate();
        WidgetStyle.apply(path, out);
        return out;
    }
}
