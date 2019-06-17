package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.common.context.SeerMsContext;
import edu.baylor.ecs.seer.common.context.SeerRequestContext;
import edu.baylor.ecs.seer.common.context.SeerSecurityContext;
import edu.baylor.ecs.seer.common.security.SecurityMethod;
import javassist.CtClass;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The SeerMsEntityContextService service constructs a
 * {@link edu.baylor.ecs.seer.common.context.SeerSecurityContext} from an initial list of
 * {@link javassist.CtClass} and a {@link SeerRequestContext}.
 *
 * @author  Jan Svacina
 * @version 1.0
 * @since   0.3.0
 */
@Service
public class SeerMsSecurityContextService {

    /**
     * This method returns a {@link SeerSecurityContext} populated from the global
     * {@link List} of {@link CtClass} objects and a {@link SeerRequestContext}.
     *
     * @param ctClasses the global {@link List} of {@link CtClass} objects
     * @param req the {@link SeerRequestContext} containing the path to the project to analyze
     *
     * @return a {@link SeerContext} populated with {@link SeerMsContext} objects
     */
    SeerSecurityContext getMsSeerSecurityContext(List<CtClass> ctClasses, SeerRequestContext req) {

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
