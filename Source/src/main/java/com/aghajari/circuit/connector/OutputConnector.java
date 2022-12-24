package com.aghajari.circuit.connector;

import com.aghajari.circuit.CircuitElement;
import com.aghajari.circuit.elements.WidgetStyle;
import com.aghajari.circuit.elements.Wire;
import javafx.geometry.Bounds;

public class OutputConnector extends Connector {

    private InputConnector connection;

    @Override
    public boolean hasConnected() {
        return connection != null;
    }

    public InputConnector getConnection() {
        return connection;
    }

    public boolean connect(InputConnector connection) {
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

        CircuitElement owner = connection.getOwner();
        if (owner instanceof Wire) {
            double xPadding = ((Wire) owner).isInternalGateWire() ? 0 : WidgetStyle.RADIUS;
            ((Wire) owner).setStartX(targetBound.getCenterX() + xPadding);
            ((Wire) owner).setStartY(targetBound.getCenterY());

        } else {
            updateGateBounds(targetBound, bounds, owner);
        }

        owner.requestUpdate(connection);
    }

    @Override
    protected void requestDisconnect() {
        connection = null;
        notifyConnectionChanged();
    }

    void requestConnect(InputConnector inputConnector) {
        this.connection = inputConnector;
        notifyConnectionChanged();
    }

    @Override
    public boolean calculate() {
        return getOwner().calculate(this);
    }
}
