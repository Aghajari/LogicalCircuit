package com.aghajari.circuit.parser;

import java.util.*;

public class Circuit {

    private final LinkedHashSet<String> idsInOrder = new LinkedHashSet<>();
    private final List<Element> elements = new ArrayList<>();
    private final Map<String, Circuit> modules = new HashMap<>();

    private String id;
    private String name;

    public LinkedHashSet<String> getIdsInOrder() {
        return idsInOrder;
    }

    public List<Element> getElements() {
        return elements;
    }

    public void addModule(Circuit circuit) {
        modules.put(circuit.getId(), circuit);
    }

    public Map<String, Circuit> getModules() {
        return modules;
    }

    public Circuit getModule(String id) {
        return modules.get(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
