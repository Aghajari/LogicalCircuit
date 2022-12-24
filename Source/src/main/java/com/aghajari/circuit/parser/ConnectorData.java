package com.aghajari.circuit.parser;

public class ConnectorData {

    private boolean isOutput;
    private boolean hasConnected;
    private int index;
    private int connectionIndex;
    private String connectionElementId;

    public boolean isOutput() {
        return isOutput;
    }

    public void setIsOutput(boolean output) {
        isOutput = output;
    }

    public boolean hasConnected() {
        return hasConnected;
    }

    public void setHasConnected(boolean hasConnected) {
        this.hasConnected = hasConnected;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getConnectionIndex() {
        return connectionIndex;
    }

    public void setConnectionIndex(int connectionIndex) {
        this.connectionIndex = connectionIndex;
    }

    public String getConnectionElementId() {
        return connectionElementId;
    }

    public void setConnectionElementId(String connectionElementId) {
        this.connectionElementId = connectionElementId;
    }
}
