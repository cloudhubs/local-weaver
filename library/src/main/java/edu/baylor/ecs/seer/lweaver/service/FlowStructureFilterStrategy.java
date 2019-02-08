package edu.baylor.ecs.seer.lweaver.service;

import javassist.CtClass;

public interface FlowStructureFilterStrategy {

    boolean doFilter(CtClass clazz);

}
