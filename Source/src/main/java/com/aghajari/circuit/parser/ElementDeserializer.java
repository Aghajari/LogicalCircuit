package com.aghajari.circuit.parser;

import com.aghajari.circuit.elements.Module;
import com.aghajari.circuit.elements.NumberGate;
import com.aghajari.circuit.elements.Wire;
import com.google.gson.*;

import java.lang.reflect.Type;

class ElementDeserializer implements JsonDeserializer<Element> {

    @Override
    public Element deserialize(JsonElement jsonElement,
                               Type type,
                               JsonDeserializationContext jsonDeserializationContext
    ) throws JsonParseException {
        String cls = jsonElement.getAsJsonObject()
                .get("elementClass")
                .getAsString();

        if (Wire.class.getName().equals(cls)) {
            return new Gson().fromJson(jsonElement, WireElement.class);
        } else if (Module.class.getName().equals(cls)) {
            return new Gson().fromJson(jsonElement, ModuleElement.class);
        } else if (NumberGate.class.getName().equals(cls)) {
            return new Gson().fromJson(jsonElement, NumberGateElement.class);
        } else {
            return new Gson().fromJson(jsonElement, GateElement.class);
        }
    }
}