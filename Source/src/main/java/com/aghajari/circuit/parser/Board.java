package com.aghajari.circuit.parser;

import com.aghajari.circuit.CircuitBoard;
import com.aghajari.circuit.CircuitElement;
import com.aghajari.circuit.connector.Connector;
import com.aghajari.circuit.connector.InputConnector;
import com.aghajari.circuit.connector.OutputConnector;
import com.aghajari.circuit.elements.BaseGate;
import com.aghajari.circuit.elements.Module;
import com.aghajari.circuit.elements.NumberGate;
import com.aghajari.circuit.elements.Wire;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Board {

    public static void saveToFile(CircuitBoard board, File file) throws IOException {
        Files.writeString(Paths.get(file.toURI()), toJson(board));
    }

    public static Circuit toCircuit(CircuitBoard board) {
        return toCircuit(null, board);
    }

    public static Circuit toCircuit(Circuit root, CircuitBoard board) {
        Circuit circuit = new Circuit();

        circuit.getIdsInOrder().addAll(
                board.getElementsInOrder().stream()
                        .map(CircuitElement::getId)
                        .collect(Collectors.toList())
        );
        circuit.setId(board.getId());
        circuit.setName(board.getBoardName());

        board.findModules().forEach(module -> {
            Circuit target = root == null ? circuit : root;
            if (target.getModule(module.getId()) == null)
                target.addModule(toCircuit(target, module.getModuleBoard()));
        });

        board.forEachCircuitElement(element -> {
            if (element instanceof Wire && ((Wire) element).isInternalGateWire())
                return;

            circuit.getElements().add(read(element));
        });

        return circuit;
    }

    public static String toJson(CircuitBoard board) {
        return new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(toCircuit(board));
    }

    public static CircuitReader load(CircuitBoard board, File file) throws FileNotFoundException {
        Circuit circuitData = newGson().fromJson(new FileReader(file), Circuit.class);
        return parse(circuitData, board);
    }

    public static CircuitReader parse(Circuit circuitData, CircuitBoard board) {
        HashMap<String, CircuitBoard> modules = new HashMap<>();
        while (modules.size() != circuitData.getModules().size()) {
            circuitData.getModules().forEach((k, v) -> {
                boolean shouldWait = v.getElements().stream()
                        .filter(e -> e instanceof ModuleElement)
                        .map(e -> (ModuleElement) e)
                        .anyMatch(e -> e.getModuleId() != null
                                && !modules.containsKey(e.getModuleId()));
                if (!shouldWait) {
                    CircuitBoard moduleBoard = new CircuitBoard();
                    moduleBoard.setId(v.getId());
                    moduleBoard.setBoardName(v.getName());

                    parseCircuit(modules, v, moduleBoard);
                    modules.put(k, moduleBoard);
                }
            });
        }

        parseCircuit(modules, circuitData, board);
        return new CircuitReader(circuitData, modules);
    }

    private static void parseCircuit(
            HashMap<String, CircuitBoard> modules,
            Circuit circuitData,
            CircuitBoard board
    ) {
        HashMap<String, CircuitElement> circuitElements = new HashMap<>();
        circuitData.getElements().forEach(element -> {
            try {
                Class<?> cls = Class.forName(element.getElementClass());
                CircuitElement circuit = (CircuitElement) cls.getDeclaredConstructor().newInstance();
                circuit.setElementName(element.getElementName());

                if (cls == Wire.class) {
                    WireElement data = (WireElement) element;
                    Wire wire = (Wire) circuit;
                    wire.setInputValue(data.isInputValue());
                    wire.setId(data.getElementId());
                    board.getChildren().add(wire);

                    wire.setStartX(data.getStartX());
                    wire.setStartY(data.getStartY());
                    wire.setEndX(data.getEndX());
                    wire.setEndY(data.getEndY());
                } else {
                    GateElement data = (GateElement) element;
                    BaseGate gate = (BaseGate) circuit;
                    gate.setId(data.getElementId());
                    gate.setLayoutX(data.getLayoutX());
                    gate.setLayoutY(data.getLayoutY());
                    if (!gate.isPrefSizeFixed()) {
                        gate.setPrefWidth(data.getPrefWidth());
                        gate.setPrefHeight(data.getPrefHeight());
                    }
                    gate.requestUpdate();
                    board.getChildren().add(gate);

                    if (element instanceof ModuleElement) {
                        ModuleElement moduleElement = (ModuleElement) element;
                        if (moduleElement.getModuleId() != null) {
                            ((Module) circuit).setModuleBoard(modules.get(moduleElement.getModuleId()));
                        }
                    }

                    if (element instanceof NumberGateElement) {
                        NumberGateElement numberGateElement = (NumberGateElement) element;
                        NumberGate numberGate = (NumberGate) circuit;
                        numberGate.setNumber(numberGateElement.getNumber());
                        numberGate.setBitCount(numberGateElement.getBitCount());
                    }

                    List<Wire> internalWires = gate.getInternalWires();
                    if (internalWires.size() != data.getInternalWires().size()) {
                        System.err.println("Expected " +
                                data.getInternalWires().size() +
                                " Internal Wires but got " +
                                internalWires.size() +
                                ": " + data.getElementId() +
                                " (" + data.getElementName() + ")");
                    }
                    for (int i = 0; i < internalWires.size(); i++) {
                        Wire wire = internalWires.get(i);
                        WireElement wireData = data.getInternalWires().get(i);

                        wire.setInputValue(wireData.isInputValue());
                        wire.setId(wireData.getElementId());
                        wire.setElementName(wireData.getElementName());

                        wire.setStartX(wireData.getStartX());
                        wire.setStartY(wireData.getStartY());
                        wire.setEndX(wireData.getEndX());
                        wire.setEndY(wireData.getEndY());
                        circuitElements.put(wireData.getElementId(), wire);
                    }
                }

                circuitElements.put(element.getElementId(), circuit);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        circuitData.getElements().forEach(element -> {
            CircuitElement owner = circuitElements.get(element.getElementId());
            List<OutputConnector> outputConnectors = owner.getAllOutputConnectors();
            List<InputConnector> inputConnectors = owner.getAllInputConnectors();

            element.getConnectors().forEach(connectorData -> {
                try {
                    if (connectorData.hasConnected()) {
                        CircuitElement stack = circuitElements.get(connectorData.getConnectionElementId());
                        if (stack == null) {
                            System.err.println("Couldn't find " + connectorData.getConnectionElementId());
                            return;
                        }

                        if (connectorData.isOutput()) {
                            OutputConnector outputConnector = outputConnectors.get(connectorData.getIndex());
                            if (!outputConnector.hasConnected()) {
                                outputConnector.connect(
                                        stack.getAllInputConnectors()
                                                .get(connectorData.getConnectionIndex())
                                );
                            }
                        } else {
                            InputConnector inputConnector = inputConnectors.get(connectorData.getIndex());
                            if (!inputConnector.hasConnected()) {
                                inputConnector.connect(
                                        stack.getAllOutputConnectors()
                                                .get(connectorData.getConnectionIndex())
                                );
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        circuitData.getIdsInOrder().forEach(id -> {
            CircuitElement e = circuitElements.get(id);
            if (e != null)
                board.getElementsInOrder().add(e);
        });
    }

    private static Element read(CircuitElement element) {
        Element output;
        if (element instanceof Wire) {
            Wire wire = (Wire) element;
            WireElement wireContent = new WireElement();
            wireContent.setStartX(wire.getStartX());
            wireContent.setStartY(wire.getStartY());
            wireContent.setEndX(wire.getEndX());
            wireContent.setEndY(wire.getEndY());
            wireContent.setInputValue(wire.isInputValue());

            output = wireContent;
        } else if (element instanceof Module) {
            Module module = (Module) element;
            ModuleElement moduleContent = new ModuleElement();
            if (module.getModuleBoard() != null) {
                moduleContent.setModuleId(module.getModuleBoard().getId());
            }
            output = moduleContent;
        } else if (element instanceof NumberGate) {
            NumberGate numberGate = (NumberGate) element;
            NumberGateElement numberGateContent = new NumberGateElement();
            numberGateContent.setNumber(numberGate.getNumber());
            numberGateContent.setBitCount(numberGate.getBitCount());
            output = numberGateContent;
        } else {
            output = new GateElement();
        }

        if (output instanceof GateElement) {
            BaseGate gate = (BaseGate) element;
            GateElement gateContent = (GateElement) output;
            gateContent.setLayoutX(gate.getLayoutX());
            gateContent.setLayoutY(gate.getLayoutY());
            gateContent.setPrefWidth(gate.getPrefWidth());
            gateContent.setPrefHeight(gate.getPrefHeight());
            gate.getInternalWires().forEach(wire ->
                    gateContent.getInternalWires().add((WireElement) read(wire)));
        }

        output.setElementName(element.getElementName());
        output.setElementClass(element.getClass().getName());
        output.setElementId(element.getId());

        List<InputConnector> inputConnectors = element.getAllInputConnectors();
        for (int i = 0; i < inputConnectors.size(); i++) {
            output.getConnectors().add(read(inputConnectors.get(i), i));
        }

        List<OutputConnector> outputConnectors = element.getAllOutputConnectors();
        for (int i = 0; i < outputConnectors.size(); i++) {
            output.getConnectors().add(read(outputConnectors.get(i), i));
        }

        return output;
    }

    @SuppressWarnings("ConstantConditions")
    private static ConnectorData read(Connector connector, int index) {
        ConnectorData data = new ConnectorData();
        data.setIsOutput(connector instanceof OutputConnector);
        data.setHasConnected(connector.hasConnected());
        data.setIndex(index);
        if (data.hasConnected()) {
            if (data.isOutput()) {
                OutputConnector outputConnector = (OutputConnector) connector;
                data.setConnectionElementId(
                        outputConnector.getConnection()
                                .getOwner()
                                .getId()
                );
                data.setConnectionIndex(
                        outputConnector.getConnection()
                                .getOwner()
                                .getAllInputConnectors()
                                .indexOf(outputConnector.getConnection())
                );
            } else {
                InputConnector inputConnector = (InputConnector) connector;
                data.setConnectionElementId(
                        inputConnector.getConnection()
                                .getOwner()
                                .getId()
                );
                data.setConnectionIndex(
                        inputConnector.getConnection()
                                .getOwner()
                                .getAllOutputConnectors()
                                .indexOf(inputConnector.getConnection())
                );
            }
        }
        return data;
    }

    private static Gson newGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Element.class, new ElementDeserializer())
                .create();
    }
}
