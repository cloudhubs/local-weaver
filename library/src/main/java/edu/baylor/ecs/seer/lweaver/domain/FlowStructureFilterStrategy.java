package edu.baylor.ecs.seer.lweaver.domain;

import javassist.CtClass;

public interface FlowStructureFilterStrategy {

    boolean doFilter(CtClass clazz);

}
