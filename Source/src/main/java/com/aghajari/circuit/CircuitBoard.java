package com.aghajari.circuit;

import com.aghajari.circuit.connector.InputConnector;
import com.aghajari.circuit.connector.OutputConnector;
import com.aghajari.circuit.elements.modules.DLatch;
import com.aghajari.circuit.elements.Module;
import com.aghajari.circuit.elements.WidgetStyle;
import com.aghajari.circuit.elements.Wire;
import com.aghajari.circuit.elements.modules.flipflop.BaseFlipFlop;
import com.aghajari.circuit.parser.Board;
import com.aghajari.circuit.parser.CircuitReader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CircuitBoard extends AnchorPane {

    private String boardName;
    private final List<CircuitElement> elementsInOrder = new LinkedList<>();

    public CircuitBoard() {
        setOnMouseMoved(e -> {
            for (Node node : getChildren()) {
                if (node instanceof CircuitElement) {
                    List<Node> list = ((CircuitElement) node).getMovableNodes();
                    if (checkBounds(e, node, list)) return;
                }
            }

            for (Node node : getChildren()) {
                if (node instanceof CircuitElement) {
                    List<Node> list = ((CircuitElement) node).getClickableNodes();
                    if (checkBounds(e, node, list)) return;
                }
            }
        });
        setId(rndId());
    }

    private boolean checkBounds(MouseEvent e, Node node, List<Node> list) {
        for (Node n2 : list) {
            Bounds bounds = n2.localToScene(n2.getBoundsInLocal());
            if (bounds.contains(e.getSceneX(), e.getSceneY())) {
                getChildren().remove(node);
                getChildren().add(node);
                return true;
            }
        }
        return false;
    }

    public List<Module> findModules() {
        return elementsInOrder.stream()
                .filter(c -> c instanceof Module)
                .map(c -> (Module) c)
                .collect(Collectors.toList());
    }

    public void updateModules() {
        getElementsInOrder().stream()
                .filter(e -> e instanceof Module)
                .map(e -> (Module) e)
                .collect(Collectors.toList())
                .forEach(module -> {
                    module.setElementName(module.getElementName());
                    module.requestUpdate();
                });
    }

    public void forEachCircuitElement(Consumer<CircuitElement> circuitElementConsumer) {
        getChildren().stream()
                .filter(node -> node instanceof CircuitElement)
                .map(node -> (CircuitElement) node)
                .forEach(circuitElementConsumer);
    }

    public CircuitElement getElement(String name) {
        for (Node node : getChildren()) {
            if (node instanceof CircuitElement) {
                if (((CircuitElement) node).getElementName().equals(name))
                    return (CircuitElement) node;
            }
        }
        return null;
    }

    public CircuitElement getElementById(String id) {
        for (Node node : getChildren()) {
            if (node instanceof CircuitElement) {
                if (node.getId().equals(id))
                    return (CircuitElement) node;
            }
        }
        return null;
    }

    public boolean hasElement(String name) {
        for (Node node : getChildren()) {
            if (node instanceof CircuitElement) {
                if (name.equals(((CircuitElement) node).getElementName()))
                    return true;
            }
        }
        return false;
    }

    public void addElement(CircuitElement element) {
        ((Node) element).setId(rndId());
        getChildren().add((Node) element);
        elementsInOrder.add(element);
        element.setSelectorsVisible(true);
    }

    public void removeElement(String name) {
        removeElement(getElement(name));
    }

    public void removeElement(CircuitElement element) {
        if (element == null) return;
        element.remove();
        elementsInOrder.remove(element);
        //noinspection SuspiciousMethodCalls
        getChildren().remove(element);
    }

    public void findConnectionForInput(CircuitElement element) {
        List<InputConnector> inputConnectors = element.getAllEmptyInputConnectors();
        if (inputConnectors.isEmpty()) return;

        forEachCircuitElement(e -> {
            if (e == element) return;

            for (OutputConnector c : e.getAllEmptyOutputConnectors()) {
                for (InputConnector inputConnector : inputConnectors) {
                    if (c.validateConnection(inputConnector)
                            && c.connect(inputConnector)) {
                        return;
                    }
                }
            }
        });
    }

    public void findConnectionForOutput(CircuitElement element) {
        List<OutputConnector> outputConnectors = element.getAllEmptyOutputConnectors();
        if (outputConnectors.isEmpty()) return;

        forEachCircuitElement(e -> {
            if (e == element) return;

            for (InputConnector c : e.getAllEmptyInputConnectors()) {
                for (OutputConnector outputConnector : outputConnectors) {
                    if (c.validateConnection(outputConnector)
                            && c.connect(outputConnector)) {
                        return;
                    }
                }
            }
        });
    }

    public void requestSelectors(CircuitElement element) {
        forEachCircuitElement(e -> {
            if (e == element) return;
            e.setSelectorsVisible(false);
        });
    }

    public List<CircuitElement> getElementsInOrder() {
        return elementsInOrder;
    }

    public void saveToFile(File file) {
        try {
            Board.saveToFile(this, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CircuitReader loadFromFile(File file) {
        try {
            return Board.load(this, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }

    public CircuitBoard duplicate() {
        CircuitBoard newBoard = new CircuitBoard();
        newBoard.setBoardName(getBoardName() + " - Copy");
        Board.parse(Board.toCircuit(this), newBoard);
        return newBoard;
    }

    public void sendToBack(CircuitElement element) {
        if (elementsInOrder.remove(element))
            elementsInOrder.add(0, element);
    }

    public void bringToFront(CircuitElement element) {
        if (elementsInOrder.remove(element))
            elementsInOrder.add(element);
    }

    public TruthTable generateTruthTable() {
        if (getElementsInOrder().stream()
                .anyMatch(element -> element instanceof BaseFlipFlop
                        || element instanceof DLatch))
            return null;

        List<Wire> allWires = getElementsInOrder().stream()
                .filter(element -> element instanceof Wire)
                .map(element -> (Wire) element)
                .filter(wire -> !wire.isInternalGateWire())
                .filter(wire -> wire.getElementName() != null)
                .collect(Collectors.toList());

        if (allWires.size() <= 1)
            return null;

        List<Wire> inputWires = allWires.stream()
                .filter(wire -> !wire.getInputConnector().hasConnected())
                .collect(Collectors.toList());

        if (inputWires.isEmpty() || inputWires.size() >= 30)
            return null;

        List<Boolean> defInputValues = inputWires.stream()
                .map(Wire::isInputValue)
                .collect(Collectors.toList());

        WidgetStyle.isEnabled = false;

        int sizeOfInputs = inputWires.size();
        int sizeOfTable = (int) Math.pow(2, sizeOfInputs);
        int sizeOfWires = allWires.size();

        TruthTable truthTable = new TruthTable(
                allWires.stream()
                        .map(Wire::getElementName)
                        .collect(Collectors.toList()),
                new ArrayList<>(sizeOfTable)
        );

        for (int i = 1; i <= sizeOfTable; i++) {
            List<Boolean> list = new ArrayList<>(sizeOfWires);
            truthTable.getTruthTable().add(list);

            int twoPow = sizeOfTable;
            for (Wire inputWire : inputWires) {
                twoPow /= 2;

                boolean input = ((int) Math.ceil(i * 1.0 / twoPow)) % 2 == 0;
                inputWire.setInputValue(input);
            }

            allWires.forEach(wire -> list.add(wire.calculate(null)));
        }

        for (int i = 0; i < sizeOfInputs; i++) {
            inputWires.get(i).setInputValue(defInputValues.get(i));
        }
        WidgetStyle.isEnabled = true;
        return truthTable;
    }

    private static final Random rnd = new SecureRandom();
    private static final char[] symbols = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "abcdefghijklmnopqrstuvwxyz" +
            "0123456789").toCharArray();

    private static String rndId() {
        StringBuilder builder = new StringBuilder(16);
        for (int idx = 0; idx < 16; ++idx)
            builder.append(symbols[rnd.nextInt(symbols.length)]);
        return builder.toString();
    }
}
