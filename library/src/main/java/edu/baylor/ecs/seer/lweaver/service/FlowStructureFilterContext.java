package edu.baylor.ecs.seer.lweaver.service;

import javassist.CtClass;

public class FlowStructureFilterContext {

    private FlowStructureFilterStrategy strategy;

    public FlowStructureFilterContext(FlowStructureFilterStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean doFilter(CtClass clazz) {
        return strategy.doFilter(clazz);
    }

}
