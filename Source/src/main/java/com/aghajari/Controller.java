package com.aghajari;

import com.aghajari.circuit.CircuitBoard;
import com.aghajari.circuit.CircuitElement;
import com.aghajari.circuit.Gate;
import com.aghajari.circuit.TruthTable;
import com.aghajari.circuit.connector.OnConnectionChangedListener;
import com.aghajari.circuit.elements.Module;
import com.aghajari.circuit.elements.*;
import com.aghajari.circuit.elements.modules.DLatch;
import com.aghajari.circuit.elements.modules.NumberToSevenSegment;
import com.aghajari.circuit.elements.modules.flipflop.DFlipFlop;
import com.aghajari.circuit.elements.modules.flipflop.NegativeDFlipFlop;
import com.aghajari.circuit.elements.modules.flipflop.NegativeTFlipFlop;
import com.aghajari.circuit.elements.modules.flipflop.TFlipFlop;
import com.aghajari.circuit.elements.modules.SevenSegment;
import com.aghajari.circuit.parser.CircuitReader;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public AnchorPane root;

    @FXML
    public MenuBar menuBar;

    public List<CircuitBoard> boards = new LinkedList<>();
    private CircuitBoard board;

    private Timeline runTimeline;

    private final Menu file = new Menu("File");
    private final Menu tabs = new Menu("Tabs");
    private final Menu newElement = new Menu("New");
    private final Menu elements = new Menu("Elements");
    private final Menu input = new Menu("Input");
    private final Menu output = new Menu("Output");
    private final Menu run = new Menu("Run");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        createNewTab("Main", new CircuitBoard(), true);

        MenuItem runItem = new MenuItem("Run");
        runItem.setOnAction(e -> {
            if (board == null) return;
            StringBuilder out = new StringBuilder();
            board.forEachCircuitElement(element ->
                    out.append(element.getElementName())
                            .append(" (")
                            .append(element.getId())
                            .append(") : ")
                            .append(element.calculate(null))
                            .append('\n'));
            System.out.println(out);
        });
        run.getItems().add(runItem);

        input.setOnShowing(e -> input.getItems().forEach(item -> {
            CircuitElement element = board.getElementById(item.getId());
            ((CheckMenuItem) item).setSelected(element != null && ((Wire) element).isInputValue());
            item.setVisible(element != null && !element.getAllEmptyInputConnectors().isEmpty());
        }));

        output.setOnShowing(e -> output.getItems().forEach(item -> {
            CheckMenuItem checkMenuItem = (CheckMenuItem) item;
            CircuitElement element = board.getElementById(item.getId());
            checkMenuItem.setVisible(element != null && !element.getAllEmptyOutputConnectors().isEmpty());
            checkMenuItem.setSelected(element != null && element.calculate(null));
        }));

        Menu moduleItem = new Menu("Module");
        addModule(moduleItem);
        addNumberGate(moduleItem);
        addGate(SevenSegment.class, "7Segment", moduleItem);
        addGate(NumberToSevenSegment.class, "Number To 7Segment", moduleItem);
        addGate(DLatch.class, "D-Latch", moduleItem);

        Menu flipFlop = new Menu("FlipFlop");
        addGate(DFlipFlop.class, "D-FlipFlop (Positive)", flipFlop);
        addGate(NegativeDFlipFlop.class, "D-FlipFlop (Negative)", flipFlop);
        addGate(TFlipFlop.class, "T-FlipFlop (Positive)", flipFlop);
        addGate(NegativeTFlipFlop.class, "T-FlipFlop (Negative)", flipFlop);
        moduleItem.getItems().add(flipFlop);
        newElement.getItems().add(moduleItem);

        addWire();
        addGate(Plug.class, "Plug");
        addGate(GateAND.class, "AND");
        addGate(GateOR.class, "OR");
        addGate(GateXOR.class, "XOR");
        addGate(GateNOT.class, "NOT");
        addGate(GateNAND.class, "NAND");
        addGate(GateNOR.class, "NOR");
        addGate(GateXNOR.class, "XNOR");

        MenuItem save = new MenuItem("Save");
        save.setOnAction(e -> {
            File file = createFileChooser(true)
                    .showSaveDialog(root.getScene().getWindow());
            if (file == null) return;

            board.saveToFile(file);
        });

        MenuItem load = new MenuItem("Load");
        load.setOnAction(e -> {
            File file = createFileChooser(false)
                    .showOpenDialog(root.getScene().getWindow());
            if (file == null) return;

            String name = file.getName().replace(".circuit", "");
            createNewTab(name, new CircuitBoard(), true);

            CircuitReader circuitReader = board.loadFromFile(file);
            circuitReader.idsToElements(board)
                    .forEach(this::addElementToMenu);
            circuitReader.getModules().values()
                    .forEach(m -> createNewTab(m.getBoardName(), m, false));

            run();
        });

        MenuItem truthTable = new MenuItem("Truth Table");
        truthTable.setOnAction(e -> showTruthTable(board));

        MenuItem tabTitle = new MenuItem();
        tabTitle.setDisable(true);
        file.setOnShown(e -> tabTitle.setText(board == null ? "No Tab" : board.getBoardName()));
        file.getItems().addAll(tabTitle, truthTable, new SeparatorMenuItem(), save, load);

        MenuItem newTab = new MenuItem("New Tab");
        newTab.setOnAction(e -> askForName("Tab")
                .ifPresent(name -> createNewTab(name, new CircuitBoard(), true)));
        tabs.getItems().add(0, newTab);
        tabs.getItems().add(1, new SeparatorMenuItem());

        menuBar.getMenus().addAll(file, tabs, newElement, elements, input, output, run);
        menuBar.prefWidthProperty().bind(root.widthProperty());
    }

    private void addElementToMenu(CircuitElement element) {
        if (element instanceof Wire)
            addWireToInputs((Wire) element);

        OnConnectionChangedListener listener = (c) -> run();
        element.getAllInputConnectors().forEach(input ->
                input.setOnConnectionChangedListener(listener)
        );

        Menu item = new Menu(element.getElementName());

        MenuItem openModule = new MenuItem("Open Module");
        openModule.setVisible(element instanceof Module);
        openModule.setOnAction(e -> {
            if (!(element instanceof Module)) return;

            Module module = (Module) element;
            if (module.getModuleBoard() == null) return;

            if (boards.contains(module.getModuleBoard()))
                selectBoard(module.getModuleBoard());
            else
                createNewTab(
                        module.getModuleBoard().getBoardName(),
                        module.getModuleBoard(),
                        true
                );
        });

        MenuItem type = new MenuItem("Type: " + element.getClass().getSimpleName());
        type.setDisable(true);
        MenuItem id = new MenuItem("ID: " + element.getId());
        id.setDisable(true);
        MenuItem inputCount = new MenuItem("Input: ");
        inputCount.setDisable(true);
        MenuItem outputCount = new MenuItem("Output: ");
        outputCount.setDisable(true);

        MenuItem wireType = new MenuItem();
        CheckMenuItem wireInputValue = new CheckMenuItem();
        SeparatorMenuItem wireSep = new SeparatorMenuItem();
        wireType.setDisable(true);
        if (!(element instanceof Wire)) {
            wireType.setVisible(false);
            wireInputValue.setVisible(false);
            wireSep.setVisible(false);
        }

        item.setOnShowing(e -> {
            if (element instanceof Module) {
                Module module = (Module) element;
                if (module.getModuleBoard() != null) {
                    type.setText("Type: Module " + module.getModuleBoard().getBoardName());
                } else {
                    type.setText("Type: Unknown Module");
                }
            }

            inputCount.setText("Input: " +
                    element.getAllEmptyInputConnectors().size() +
                    "/" +
                    element.getAllInputConnectors().size()
            );

            int realNumberOfEmptyOutputConnectors = element instanceof BaseGate ?
                    ((BaseGate) element).getRealNumberOfEmptyOutputConnectors() :
                    element.getAllEmptyOutputConnectors().size();

            outputCount.setText("Output: " +
                    realNumberOfEmptyOutputConnectors +
                    "/" +
                    element.getAllOutputConnectors().size()
            );

            wireInputValue.setOnAction(null);
            if (element instanceof Wire) {
                Wire wire = (Wire) element;
                if (!wire.getInputConnector().hasConnected()) {
                    wireType.setText("Input Wire");
                    wireInputValue.setSelected(wire.isInputValue());
                    wireType.setVisible(true);
                    wireSep.setVisible(true);
                    wireInputValue.setVisible(true);
                    wireInputValue.setDisable(false);
                    wireInputValue.setOnAction(a -> {
                        wire.setInputValue(!wire.isInputValue());
                        wireInputValue.setSelected(wire.isInputValue());
                        run();
                    });
                } else if (!wire.getOutputConnector().hasConnected()) {
                    wireType.setText("Output Wire");
                    wireType.setVisible(true);
                    wireSep.setVisible(true);
                    wireInputValue.setVisible(true);
                    wireInputValue.setDisable(true);
                    wireInputValue.setSelected(wire.calculate(null));
                } else {
                    wireType.setVisible(false);
                    wireInputValue.setVisible(false);
                    wireSep.setVisible(false);
                }
                wireInputValue.setText(wireInputValue.isSelected() ? "True" : "False");
            }
        });

        CheckMenuItem gravity = new CheckMenuItem("Connectors Gravity");
        gravity.setSelected(true);
        gravity.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (element instanceof BaseGate) {
                ((BaseGate) element).setGravityEnabled(gravity.isSelected());
            } else if (element instanceof Wire) {
                ((Wire) element).setGravityEnabled(gravity.isSelected());
            }
        });

        MenuItem updateNumber = new MenuItem("Update Number");
        updateNumber.setVisible(element instanceof NumberGate);
        updateNumber.setOnAction(e -> {
            if (element instanceof NumberGate) {
                askForNumber(
                        "Update Number",
                        "Update Number of " + element.getElementName()
                ).ifPresent(number -> {
                    ((NumberGate) element).setNumber(number);
                    run();
                });
            }
        });

        MenuItem select = new MenuItem("Select Element");
        select.setOnAction(a -> element.setSelectorsVisible(true));

        MenuItem sendToBack = new MenuItem("Send To Back");
        sendToBack.setOnAction(a -> {
            board.sendToBack(element);

            elements.getItems().remove(item);
            elements.getItems().add(0, item);

            input.getItems().stream()
                    .filter(inputItem -> element.getId().equals(inputItem.getId()))
                    .findAny()
                    .ifPresent(inputItem -> {
                        input.getItems().remove(inputItem);
                        input.getItems().add(0, inputItem);
                    });


            output.getItems().stream()
                    .filter(outputItem -> element.getId().equals(outputItem.getId()))
                    .findAny()
                    .ifPresent(outputItem -> {
                        output.getItems().remove(outputItem);
                        output.getItems().add(0, outputItem);
                    });
        });

        MenuItem bringToFront = new MenuItem("Bring To Front");
        bringToFront.setOnAction(a -> {
            board.bringToFront(element);

            elements.getItems().remove(item);
            elements.getItems().add(item);

            input.getItems().stream()
                    .filter(inputItem -> element.getId().equals(inputItem.getId()))
                    .findAny()
                    .ifPresent(inputItem -> {
                        input.getItems().remove(inputItem);
                        input.getItems().add(inputItem);
                    });


            output.getItems().stream()
                    .filter(outputItem -> element.getId().equals(outputItem.getId()))
                    .findAny()
                    .ifPresent(outputItem -> {
                        output.getItems().remove(outputItem);
                        output.getItems().add(outputItem);
                    });
        });

        MenuItem disconnectInputs = new MenuItem("Disconnect Inputs");
        disconnectInputs.setOnAction(e -> {
            element.disconnectInputConnectors();
            run();
        });

        MenuItem disconnectOutputs = new MenuItem("Disconnect Outputs");
        disconnectOutputs.setOnAction(e -> {
            element.disconnectOutputConnectors();
            run();
        });

        MenuItem reset = new MenuItem("Reset");
        reset.setOnAction(e -> {
            element.disconnectAllConnectors();
            placeElement(element);
            if (element instanceof BaseGate) {
                ((BaseGate) element).requestUpdate();
            }
            run();
        });

        MenuItem remove = new MenuItem("Remove");
        remove.setOnAction(e -> {
            board.removeElement(element);
            elements.getItems().remove(item);

            if (element instanceof Wire) {
                input.getItems().removeIf(inputItem ->
                        element.getId().equals(inputItem.getId()));

                output.getItems().removeIf(inputItem ->
                        element.getId().equals(inputItem.getId()));
            }
            run();
        });

        MenuItem rename = new MenuItem("Rename");
        rename.setOnAction(e -> {
            Optional<String> value = askForName(element.getElementName());
            if (value.isPresent() && !board.hasElement(value.get())) {
                element.setElementName(value.get());
                item.setText(value.get());
                input.getItems().stream()
                        .filter(inputItem -> element.getId().equals(inputItem.getId()))
                        .findAny()
                        .ifPresent(inputItem -> inputItem.setText(value.get()));
            }
        });

        remove.setStyle("-fx-text-fill: red;");
        item.getItems().addAll(
                openModule, type, id, inputCount, outputCount, new SeparatorMenuItem(),
                wireType, wireInputValue, wireSep,
                gravity, updateNumber, select, sendToBack, bringToFront,
                disconnectInputs, disconnectOutputs, reset,
                rename, new SeparatorMenuItem(), remove
        );
        elements.getItems().add(item);
    }

    private void addWire() {
        MenuItem wireItem = new MenuItem("Wire");
        wireItem.setOnAction(actionEvent -> {
            if (board == null) return;

            Optional<String> value = askForName("Wire");
            if (value.isPresent() && !board.hasElement(value.get())) {
                Wire wire = new Wire();
                wire.setElementName(value.get());
                wire.setInputValue(true);
                wire.calculate(null);

                addNewElement(wire);
            }
        });
        newElement.getItems().add(wireItem);
    }

    private void addWireToInputs(Wire wire) {
        CheckMenuItem inputValue = new CheckMenuItem(wire.getElementName());
        inputValue.setOnAction(e -> {
            wire.setInputValue(!wire.isInputValue());
            inputValue.setSelected(wire.isInputValue());
            run();
        });
        inputValue.setId(wire.getId());
        inputValue.setSelected(wire.isInputValue());
        input.getItems().add(inputValue);

        CheckMenuItem outputValue = new CheckMenuItem();
        outputValue.textProperty().bind(inputValue.textProperty());
        outputValue.setId(wire.getId());
        outputValue.setDisable(true);
        output.getItems().add(outputValue);
    }

    private void addGate(Class<? extends Gate> cls, String text) {
        addGate(cls, text, newElement);
    }

    private void addGate(Class<? extends Gate> cls, String text, Menu menu) {
        MenuItem gateItem = new MenuItem(text);
        gateItem.setOnAction(actionEvent -> {
            if (board == null) return;
            Optional<String> value = askForName(text);
            if (value.isPresent() && !board.hasElement(value.get())) {
                try {
                    Gate gate = cls.getDeclaredConstructor().newInstance();
                    gate.setElementName(value.get());
                    addNewElement(gate);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        menu.getItems().add(gateItem);
    }

    private void addNumberGate(Menu menu) {
        MenuItem gateItem = new MenuItem("Number");
        gateItem.setOnAction(actionEvent -> {
            if (board == null) return;

            Optional<String> name = askForName("Number Gate");
            if (name.isEmpty() || board.hasElement(name.get())) return;

            Optional<Integer> bit = askForNumber("Count of bits", "Enter count of bits");
            if (bit.isEmpty()) return;

            Optional<Integer> number = askForNumber("Enter number", "Enter number of gate");
            if (number.isEmpty()) return;

            NumberGate numberGate = new NumberGate();
            numberGate.setElementName(name.get());
            numberGate.setNumber(number.get());
            numberGate.setBitCount(bit.get());
            addNewElement(numberGate);
        });
        menu.getItems().add(gateItem);
    }

    private void addModule(Menu menu) {
        Menu myModules = new Menu("My Modules");
        tabs.getItems().addListener((ListChangeListener<MenuItem>) change -> {
            myModules.getItems().clear();

            tabs.getItems().forEach(tab -> {
                if (tab.getId() == null) return;

                MenuItem item = new MenuItem(tab.getText());
                item.setId(tab.getId());
                item.setOnAction(e -> {
                    Optional<String> value = askForName("Module");
                    if (value.isPresent() && !board.hasElement(value.get())) {
                        Module module = new Module();
                        module.setElementName(value.get());
                        module.setModuleBoard(
                                boards.stream()
                                        .filter(board -> board.getId().equals(item.getId()))
                                        .findAny().orElse(null)
                        );
                        addNewElement(module);
                    }
                });
                myModules.getItems().add(item);
            });
        });
        myModules.setOnShown(e -> myModules.getItems().forEach(item -> {
            if (board == null || item.getId().equals(board.getId())) {
                item.setVisible(false);
                return;
            }

            CircuitBoard targetBoard = boards.stream()
                    .filter(b -> b.getId().equals(item.getId()))
                    .findAny().orElse(null);
            if (targetBoard == null) {
                item.setVisible(false);
            } else {
                Optional<CircuitBoard> moduleBoard =
                        targetBoard.findModules().stream()
                                .map(Module::getModuleBoard)
                                .filter(m -> m.getId().equals(board.getId()))
                                .findAny();
                item.setVisible(moduleBoard.isEmpty());
            }
        }));
        menu.getItems().add(myModules);
    }

    private void addNewElement(CircuitElement element) {
        placeElement(element);
        board.addElement(element);
        addElementToMenu(element);
        run();
    }

    private void placeElement(CircuitElement element) {
        if (element instanceof Wire) {
            Wire wire = (Wire) element;
            wire.setStartX(50);
            wire.setStartY(50);
            wire.setEndY(50);
            wire.setEndX(200);
        } else {
            ((Node) element).setLayoutX(50);
            ((Node) element).setLayoutY(80);
        }
    }

    private Optional<String> askForName(String type) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Enter Name of " + type);
        dialog.setTitle((type.equals("Tab") ? type : "Element") + " Name");
        Optional<String> o = dialog.showAndWait();
        if (o.isPresent() && o.get().isEmpty()) {
            return Optional.empty();
        } else {
            return o;
        }
    }

    private Optional<Integer> askForNumber(String title, String headerText) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(headerText);
        dialog.setTitle(title);
        dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                dialog.getEditor().setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        Optional<String> o = dialog.showAndWait();
        if (o.isEmpty()) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(Integer.valueOf(o.get()));
            } catch (Exception ignore) {
                return Optional.empty();
            }
        }
    }

    private FileChooser createFileChooser(boolean save) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle((save ? "Save" : "Load") + " LogicalCircuit");
        fileChooser.setInitialFileName((save ? board.getBoardName() : "LogicalCircuit") + ".circuit");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("LogicalCircuit", "*.circuit"));
        return fileChooser;
    }

    private void run() {
        if (runTimeline != null)
            runTimeline.stop();

        runTimeline = new Timeline(
                new KeyFrame(
                        Duration.millis(10),
                        e -> {
                            runTimeline = null;
                            run.getItems().get(0).getOnAction().handle(null);
                        }
                )
        );
        runTimeline.play();
    }

    private void createNewTab(String name, CircuitBoard tabBoard, boolean select) {
        tabBoard.setBoardName(name);

        MenuItem id = new MenuItem("ID: " + tabBoard.getId());
        id.setDisable(true);

        Menu tabItem = new Menu(name);
        tabItem.setId(tabBoard.getId());

        MenuItem open = new MenuItem("Open");
        open.setOnAction(e -> selectBoard(tabBoard));

        MenuItem close = new MenuItem("Close");
        close.setOnAction(e -> {
            boards.remove(tabBoard);
            tabs.getItems().remove(tabItem);

            if (board == tabBoard) {
                selectBoard(boards.isEmpty() ? null : boards.get(0));
            }
        });

        MenuItem rename = new MenuItem("Rename");
        rename.setOnAction(e -> askForName("Tab").ifPresent(newName -> {
            tabItem.setText(newName);
            tabBoard.setBoardName(newName);
            board.findModules().stream()
                    .filter(m -> m.getModuleBoard() == tabBoard)
                    .forEach(Module::requestUpdateText);

            // force update modules tab
            int index = tabs.getItems().indexOf(tabItem);
            tabs.getItems().remove(tabItem);
            tabs.getItems().add(index, tabItem);
        }));

        MenuItem duplicate = new MenuItem("Duplicate");
        duplicate.setOnAction(e -> {
            CircuitBoard newBoard = tabBoard.duplicate();
            createNewTab(newBoard.getBoardName(), newBoard, true);
        });

        close.setStyle("-fx-text-fill: red;");
        tabItem.getItems().addAll(open, id, new SeparatorMenuItem(), rename, duplicate, close);

        tabs.getItems().add(tabItem);
        boards.add(tabBoard);
        if (select) {
            selectBoard(tabBoard);
        }
    }

    private void selectBoard(CircuitBoard board) {
        if (board == this.board) return;
        if (this.board != null) {
            root.getChildren().remove(this.board);
        }
        if (board != null) {
            root.getChildren().add(0, board);
        }
        this.board = board;

        elements.getItems().clear();
        input.getItems().clear();
        output.getItems().clear();

        if (board == null) return;

        board.getElementsInOrder().forEach(element -> {
            if (element.getElementName() == null || element.getElementName().isEmpty())
                return;

            addElementToMenu(element);
        });

        board.updateModules();
        run();
    }

    private void showTruthTable(CircuitBoard board) {
        if (board == null) return;
        TruthTable truthTable = board.generateTruthTable();
        if (truthTable == null) return;

        TableView<List<Boolean>> table = new TableView<>();
        for (int i = 0; i < truthTable.getWires().size(); i++) {
            int finalI = i;
            TableColumn<List<Boolean>, String> column = new TableColumn<>(truthTable.getWires().get(i));
            new PropertyValueFactory<String, String>("email");
            column.setCellValueFactory(cell -> {
                boolean value = cell.getValue().get(finalI);
                return new SimpleStringProperty(value ? "1" : "0");
            });
            table.getColumns().add(column);
        }
        table.setItems(FXCollections.observableList(truthTable.getTruthTable()));

        Stage stage = new Stage();
        stage.initOwner(root.getScene().getWindow());
        stage.setTitle(board.getBoardName() + " Truth Table");
        stage.setScene(new Scene(table,
                Math.min(500, table.getColumns().size() * 50),
                Math.min(500, table.getItems().size() * 30 + 50)
        ));
        stage.show();
    }
}
