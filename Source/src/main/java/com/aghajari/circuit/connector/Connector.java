package com.aghajari.circuit.connector;

import com.aghajari.circuit.CircuitElement;
import com.aghajari.circuit.Gate;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.shape.Circle;

import java.util.HashSet;

public abstract class Connector {

    protected CircuitElement owner;
    protected Circle circle;
    protected OnConnectionChangedListener onConnectionChangedListener;

    public void setOwner(CircuitElement owner, Circle circle) {
        this.owner = owner;
        this.circle = circle;
    }

    public CircuitElement getOwner() {
        return owner;
    }

    public Bounds getBounds() {
        return circle.localToScene(circle.getBoundsInLocal());
    }

    public boolean validateConnection(Connector connector) {
        return getBounds().intersects(connector.getBounds())
                && loopValidation(connector);
    }

    private boolean loopValidation(Connector connector) {
        InputConnector inputConnector = (InputConnector)
                (this instanceof InputConnector ? this : connector);
        @SuppressWarnings("ConstantConditions")
        OutputConnector outputConnector = (OutputConnector)
                (this instanceof OutputConnector ? this : connector);

        HashSet<InputConnector> set = new HashSet<>();
        outputConnector.getOwner().getAllInputConnectors()
                .forEach(i -> recursiveOwnerValidation(set, i));
        return !set.contains(inputConnector);
    }

    private void recursiveOwnerValidation(HashSet<InputConnector> set, InputConnector connector) {
        set.add(connector);

        if (connector.hasConnected()) {
            connector.getConnection().getOwner().getAllInputConnectors()
                    .forEach(i -> recursiveOwnerValidation(set, i));
        }
    }

    protected void updateGateBounds(Bounds targetBound, Bounds bounds, CircuitElement owner) {
        if (owner instanceof Gate) {
            double x = targetBound.getMinX() - bounds.getMinX();
            double y = targetBound.getMinY() - bounds.getMinY();

            // invalid :/
            if (Math.abs(x) > 100 || Math.abs(y) > 100) return;

            Node gate = (Node) owner;
            gate.setLayoutX(gate.getLayoutX() + x);
            gate.setLayoutY(gate.getLayoutY() + y);
        }
    }

    public abstract boolean hasConnected();

    protected abstract void requestUpdate();

    protected abstract void requestDisconnect();

    public abstract boolean calculate();

    public OnConnectionChangedListener getOnConnectionChangedListener() {
        return onConnectionChangedListener;
    }

    public void setOnConnectionChangedListener(OnConnectionChangedListener onConnectionChangedListener) {
        this.onConnectionChangedListener = onConnectionChangedListener;
    }

    protected void notifyConnectionChanged() {
        if (getOnConnectionChangedListener() != null) {
            getOnConnectionChangedListener().onConnectionChanged(this);
        }
    }
}
