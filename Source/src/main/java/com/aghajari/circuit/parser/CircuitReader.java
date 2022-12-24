package com.aghajari.circuit.parser;

import com.aghajari.circuit.CircuitBoard;
import com.aghajari.circuit.CircuitElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CircuitReader {

    private final Circuit circuit;
    private final Map<String, CircuitBoard> modules;

    CircuitReader(Circuit circuit, Map<String, CircuitBoard> modules) {
        this.circuit = circuit;
        this.modules = modules;
    }

    public Circuit getCircuit() {
        return circuit;
    }

    public Map<String, CircuitBoard> getModules() {
        return modules;
    }

    public List<CircuitElement> idsToElements(CircuitBoard board) {
        HashMap<String, CircuitElement> mapIdsToNames = new HashMap<>();
        board.forEachCircuitElement(element -> {
            if (element.getElementName() == null || element.getElementName().isEmpty())
                return;

            mapIdsToNames.put(element.getId(), element);
        });

        return circuit.getIdsInOrder().stream()
                .filter(mapIdsToNames::containsKey)
                .map(mapIdsToNames::get)
                .collect(Collectors.toList());
    }

}
