package edu.baylor.ecs.seer.lweaver.domain;

import edu.baylor.ecs.seer.common.security.SecurityMethod;
import javassist.CtClass;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SecurityFilterContext {

    private SecurityFilterStrategy strategy;

    public SecurityFilterContext(SecurityFilterStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean doFilter(CtClass clazz,
                            List<SecurityMethod> methods) {
        return strategy.doFilter(clazz, methods);
    }

}
