package com.aghajari.circuit.elements;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

public class WidgetStyle {

    public enum CircleType {
        INPUT, OUTPUT, SELECTOR, NOT, GATE_OUTPUT, GATE_INPUT
    }

    public static boolean isEnabled = true;

    public final static double STROKE_SIZE = 3;
    public final static double RADIUS = STROKE_SIZE * 2;
    public final static double BIG_RADIUS = RADIUS;

    public final static Paint BOARD_COLOR = Color.WHITE;
    public final static Paint SELECTOR_COLOR = Color.RED;
    public final static Paint WIRE_COLOR = Color.BLACK;
    public final static Paint WIRE_RUN_COLOR = Color.RED;
    public final static Paint GATE_COLOR = Color.BLACK;
    public final static Paint TEXT_COLOR = Color.BLACK;

    public final static Paint INPUT_CIRCLE_COLOR = Color.BLUE;
    public final static Paint OUTPUT_CIRCLE_COLOR = Color.GREEN;

    public static void apply(Circle circle, CircleType type) {
        apply(circle, type, false);
    }

    public static void apply(Circle circle, CircleType type, boolean fill) {
        switch (type) {
            case INPUT:
            case GATE_INPUT:
                circle.setStroke(WidgetStyle.INPUT_CIRCLE_COLOR);
                circle.setStrokeDashOffset(STROKE_SIZE);
                circle.setRadius(type == CircleType.GATE_INPUT ?
                        WidgetStyle.BIG_RADIUS : WidgetStyle.RADIUS);
                break;
            case OUTPUT:
            case GATE_OUTPUT:
                circle.setStroke(WidgetStyle.OUTPUT_CIRCLE_COLOR);
                circle.setRadius(type == CircleType.GATE_OUTPUT ?
                        WidgetStyle.BIG_RADIUS : WidgetStyle.RADIUS);
                break;
            case SELECTOR:
                circle.setStroke(WidgetStyle.SELECTOR_COLOR);
                circle.setRadius(WidgetStyle.RADIUS);
                break;
            case NOT:
                circle.setStroke(WidgetStyle.GATE_COLOR);
                circle.setRadius(WidgetStyle.RADIUS);
                break;
        }

        if (!fill) {
            circle.setFill(WidgetStyle.BOARD_COLOR);
        } else {
            circle.setFill(circle.getStroke());
        }
        circle.setStrokeWidth(WidgetStyle.STROKE_SIZE);
        circle.setCenterX(circle.getRadius() / 2);
        circle.setCenterY(circle.getRadius() / 2);
    }

    public static void apply(Label label) {
        label.setTextFill(TEXT_COLOR);
        label.setStyle("-fx-background-color: white;");
    }

    public static void apply(Shape shape, boolean wireRunning) {
        if (!isEnabled) return;

        shape.setStroke(wireRunning ? WidgetStyle.WIRE_RUN_COLOR : WidgetStyle.WIRE_COLOR);

        if (wireRunning && shape.getUserData() == null) {
            shape.getStrokeDashArray().setAll(20d, 15d, 10d, 20d);

            final double maxOffset =
                    shape.getStrokeDashArray().stream()
                            .reduce(0d, Double::sum);

            Timeline timeline = new Timeline(
                    new KeyFrame(
                            Duration.ZERO,
                            new KeyValue(
                                    shape.strokeDashOffsetProperty(),
                                    maxOffset,
                                    Interpolator.LINEAR
                            )
                    ),
                    new KeyFrame(
                            Duration.seconds(2),
                            new KeyValue(
                                    shape.strokeDashOffsetProperty(),
                                    0,
                                    Interpolator.LINEAR
                            )
                    )
            );
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
            shape.setUserData(timeline);

        } else if (!wireRunning && shape.getUserData() != null) {
            Timeline timeline = (Timeline) shape.getUserData();
            timeline.stop();
            shape.setUserData(null);
            shape.getStrokeDashArray().clear();
        }
    }
}
