package com.aghajari.circuit.parser;

import java.util.ArrayList;
import java.util.List;

public abstract class Element {

    private String elementName;
    private String elementClass;
    private String elementId;
    private final List<ConnectorData> connectors = new ArrayList<>();

    public List<ConnectorData> getConnectors() {
        return connectors;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getElementClass() {
        return elementClass;
    }

    public void setElementClass(String elementClass) {
        this.elementClass = elementClass;
    }

}
