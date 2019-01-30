package edu.baylor.ecs.seer.lweaver.domain;

import javassist.CtClass;

import java.util.Map;
import java.util.Set;

public interface SecurityFilterStrategy {

    boolean doFilter(CtClass clazz,
                     Map<String, Set<String>> roles,
                     Map<String, Set<String>> nodes);

}
