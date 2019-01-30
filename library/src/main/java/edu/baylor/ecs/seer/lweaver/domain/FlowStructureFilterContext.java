package edu.baylor.ecs.seer.lweaver.domain;

import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

public class FlowStructureFilterContext {

    private String config;

    public FlowStructureFilterContext(String config) {
        this.config = config;
    }

    public boolean doFilter(CtClass clazz) {

        switch (config) {
            case "Annotation":
                return serviceAnnotationFilter(clazz);
            case "Name":
                return serviceNameFilter(clazz);
        }

        return false;
    }

}
