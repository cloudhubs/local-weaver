package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.security.SecurityMethod;
import javassist.CtClass;

import java.util.Set;

public interface SecurityFilterStrategy {

    boolean doFilter(CtClass clazz,
                     Set<SecurityMethod> methods);

}
