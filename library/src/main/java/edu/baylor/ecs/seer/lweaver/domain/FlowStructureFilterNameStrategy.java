package edu.baylor.ecs.seer.lweaver.domain;

import javassist.CtClass;

public class FlowStructureFilterNameStrategy implements FlowStructureFilterStrategy {

    @Override
    public boolean doFilter(CtClass clazz) {
        return clazz.getName().toLowerCase().contains("service");
    }

}
