package edu.baylor.ecs.seer.lweaver.domain;

import javassist.CtClass;

import java.util.Map;
import java.util.Set;

public class SecurityFilterContext {

    private SecurityFilterStrategy strategy;

    public SecurityFilterContext(SecurityFilterStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean doFilter(CtClass clazz,
                            Map<String, Set<String>> roles,
                            Map<String, Set<String>> nodes) {
        return strategy.doFilter(clazz, roles, nodes);
    }

}
