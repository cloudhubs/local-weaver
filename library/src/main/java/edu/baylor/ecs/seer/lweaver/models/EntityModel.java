package edu.baylor.ecs.seer.lweaver.models;

import java.util.ArrayList;
import java.util.List;

public class EntityModel {
    private String className;
    private List<InstanceVariableModel> instanceVariables;

    public EntityModel() {
        this.className = "";
        this.instanceVariables = new ArrayList<>();
    }

    public EntityModel(String className) {
        this.className = className;
        this.instanceVariables = new ArrayList<>();
    }

    public EntityModel(String className, List<InstanceVariableModel> instanceVariables) {
        this.className = className;
        this.instanceVariables = instanceVariables;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<InstanceVariableModel> getInstanceVariables() {
        return instanceVariables;
    }

    public void setInstanceVariables(List<InstanceVariableModel> instanceVariables) {
        this.instanceVariables = instanceVariables;
    }

    public void addInstanceVariableModel(InstanceVariableModel instanceVariableModel){
        this.instanceVariables.add(instanceVariableModel);
    }
}
