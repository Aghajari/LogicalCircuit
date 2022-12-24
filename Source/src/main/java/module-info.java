module LogicalCircuit {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.prefs;
    requires com.google.gson;

    opens com.aghajari to javafx.fxml;
    opens com.aghajari.circuit to javafx.fxml;
    opens com.aghajari.circuit.elements to javafx.fxml;
    opens com.aghajari.circuit.parser to com.google.gson;

    exports com.aghajari;
    exports com.aghajari.circuit;
    exports com.aghajari.circuit.connector;
    exports com.aghajari.circuit.elements;
    exports com.aghajari.circuit.parser;
}
