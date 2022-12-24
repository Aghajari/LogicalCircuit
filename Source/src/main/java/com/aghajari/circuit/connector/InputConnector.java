package com.aghajari.circuit.connector;

import com.aghajari.circuit.CircuitElement;
import com.aghajari.circuit.elements.WidgetStyle;
import com.aghajari.circuit.elements.Wire;
import javafx.geometry.Bounds;

public class InputConnector extends Connector {

    private OutputConnector connection = null;

    @Override
    public boolean hasConnected() {
        return connection != null;
    }

    public OutputConnector getConnection() {
        return connection;
    }

    public boolean connect(OutputConnector connection) {
        this.connection = connection;
        try {
            requestUpdate();
        } catch (Exception ignore) {
            requestDisconnect();
            return false;
        }
        connection.requestConnect(this);
        notifyConnectionChanged();
        return true;
    }

    public void disconnect() {
        if (hasConnected()) {
            connection.requestDisconnect();
            connection = null;
        }
    }

    @Override
    public void requestUpdate() {
        if (!hasConnected()) return;
        Bounds targetBound = getBounds();
        Bounds bounds = connection.getBounds();
        CircuitElement stack = connection.getOwner();

        if (stack instanceof Wire) {
            ((Wire) stack).setEndX(targetBound.getCenterX() - WidgetStyle.RADIUS);
            ((Wire) stack).setEndY(targetBound.getCenterY());

        } else {
            updateGateBounds(targetBound, bounds, stack);
        }

        stack.requestUpdate(connection);
    }

    @Override
    protected void requestDisconnect() {
        connection = null;
        notifyConnectionChanged();
    }

    void requestConnect(OutputConnector outputConnector) {
        this.connection = outputConnector;
        notifyConnectionChanged();
    }

    @Override
    public boolean calculate() {
        if (!hasConnected()) return false;
        return getConnection().calculate();
    }
}
