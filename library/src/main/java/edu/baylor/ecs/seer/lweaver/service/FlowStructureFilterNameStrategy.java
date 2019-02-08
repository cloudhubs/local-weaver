package edu.baylor.ecs.seer.lweaver.service;

import javassist.CtClass;

public class FlowStructureFilterNameStrategy implements FlowStructureFilterStrategy {

    @Override
    public boolean doFilter(CtClass clazz) {
        return clazz.getName().toLowerCase().contains("service");
    }

}
