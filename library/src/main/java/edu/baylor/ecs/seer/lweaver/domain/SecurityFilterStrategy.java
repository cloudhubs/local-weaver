package edu.baylor.ecs.seer.lweaver.domain;

import edu.baylor.ecs.seer.common.security.SecurityMethod;
import javassist.CtClass;
import jdk.internal.dynalink.linker.LinkerServices;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SecurityFilterStrategy {

    boolean doFilter(CtClass clazz,
                     Set<SecurityMethod> methods);

}
