package com.aghajari.circuit;

import com.aghajari.circuit.connector.Connector;
import com.aghajari.circuit.connector.InputConnector;
import com.aghajari.circuit.connector.OutputConnector;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface CircuitElement {

    default List<InputConnector> getAllInputConnectors() {
        return Collections.emptyList();
    }

    default List<OutputConnector> getAllOutputConnectors() {
        return Collections.emptyList();
    }

    default List<InputConnector> getAllEmptyInputConnectors() {
        return getAllInputConnectors().stream()
                .filter(c -> !c.hasConnected())
                .collect(Collectors.toList());
    }

    default List<OutputConnector> getAllEmptyOutputConnectors() {
        return getAllOutputConnectors().stream()
                .filter(c -> !c.hasConnected())
                .collect(Collectors.toList());
    }

    default List<Node> getMovableNodes() {
        return Collections.emptyList();
    }

    default List<Node> getClickableNodes() {
        return Collections.emptyList();
    }

    default void requestUpdate(Connector connector) {
        Node n = (Node) this;
        Rectangle2D old2d = n.getUserData() == null ? null : (Rectangle2D) n.getUserData();
        Bounds bounds = n.localToScene(n.getBoundsInLocal());
        Rectangle2D new2d = new Rectangle2D(
                bounds.getMinX(),
                bounds.getMinY(),
                bounds.getWidth(),
                bounds.getHeight()
        );
        n.setUserData(new2d);

        if (!new2d.equals(old2d)) {
            getAllInputConnectors().forEach(i -> {
                if (i != connector && i.getConnection() != connector)
                    i.requestUpdate();
            });
            getAllOutputConnectors().forEach(i -> {
                if (i != connector && i.getConnection() != connector)
                    i.requestUpdate();
            });
        }
    }

    default CircuitBoard getBoard() {
        return (CircuitBoard) ((Node) this).getParent();
    }

    default void setSelectorsVisible(boolean visible) {
        requestSelectors(visible);
    }

    default void requestSelectors(boolean visible) {
        if (visible && getBoard() != null) {
            getBoard().requestSelectors(this);
        }
    }

    void setElementName(String name);

    String getElementName();

    boolean calculate(OutputConnector output);

    default void disconnectInputConnectors() {
        getAllInputConnectors().forEach(InputConnector::disconnect);
    }

    default void disconnectOutputConnectors() {
        getAllOutputConnectors().forEach(OutputConnector::disconnect);
    }

    default void disconnectAllConnectors() {
        disconnectInputConnectors();
        disconnectOutputConnectors();
    }

    default void remove() {
        disconnectAllConnectors();
    }

    String getId();
}
