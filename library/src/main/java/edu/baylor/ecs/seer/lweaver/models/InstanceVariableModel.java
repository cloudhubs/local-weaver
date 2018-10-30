package edu.baylor.ecs.seer.lweaver.models;

import java.util.ArrayList;
import java.util.List;

public class InstanceVariableModel {

    private String variableName;
    private List<Pair<String, String>> attributes;

    public InstanceVariableModel() {
        this.variableName = "";
        this.attributes = new ArrayList<>();
    }

    public InstanceVariableModel(String variableName) {
        this.variableName = variableName;
        this.attributes = new ArrayList<>();
    }

    public InstanceVariableModel(String variableName, List<Pair<String, String>> attributes) {
        this.variableName = variableName;
        this.attributes = attributes;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public List<Pair<String, String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Pair<String, String>> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String key, String value){
        this.attributes.add(new Pair<>(key, value));
    }
}
