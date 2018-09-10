package edu.baylor.ecs.cfgg.evaluator.service.models;

import javafx.util.Pair;
import javassist.CtMethod;

import java.util.ArrayList;
import java.util.List;

public class MethodModel {
    private CtMethod method;
    private List<Pair<String, String>> subMethods;

    public MethodModel() {
        this.method = null;
        this.subMethods = new ArrayList<>();
    }

    public MethodModel(CtMethod method) {
        this.method = method;
        this.subMethods = new ArrayList<>();
    }

    public MethodModel(CtMethod method, List<Pair<String, String>> subMethods) {
        this.method = method;
        this.subMethods = subMethods;
    }

    public CtMethod getMethod() {
        return method;
    }

    public void setMethod(CtMethod method) {
        this.method = method;
    }

    public List<Pair<String, String>> getSubMethods() {
        return subMethods;
    }

    public void setSubMethods(List<Pair<String, String>> subMethods) {
        this.subMethods = subMethods;
    }

    public void addSubMethod(String className, String methodName){
        this.subMethods.add(new Pair<>(className, methodName));
    }
}
