package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.context.SeerRequestContext;
import edu.baylor.ecs.seer.common.context.SeerSecurityContext;
import edu.baylor.ecs.seer.common.security.SecurityMethod;
import javassist.CtClass;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SeerMsSecurityContextService {

    public SeerSecurityContext getMsSeerSecurityContext(List<CtClass> ctClasses, SeerRequestContext req) {

        SeerSecurityContext securityContext = new SeerSecurityContext(req.getSecurityAnalyzerInterface());

        SecurityFilterContext securityFilterContext =
                new SecurityFilterContext(new SecurityFilterGeneralAnnotationStrategy());

        /* Security method contains: name, roles and children */
        Set<SecurityMethod> methods = new HashSet<>();

        /* ! getSecurityMethods indeed updates the set, despite the fact it retrieves nothing  */
        for ( CtClass ctClass : ctClasses ) {
            securityFilterContext.doFilter(ctClass, methods);
        }

        securityContext.setSecurityMethods(methods);

        return securityContext;
    }
}
