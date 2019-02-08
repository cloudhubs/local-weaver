package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.context.SeerSecurityContext;
import edu.baylor.ecs.seer.common.security.SecurityMethod;
import edu.baylor.ecs.seer.lweaver.domain.SecurityFilterContext;
import edu.baylor.ecs.seer.lweaver.domain.SecurityFilterGeneralAnnotationStrategy;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.ClassFile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SeerMsSecurityContextService {

    public SeerSecurityContext getMsSeerSecurityContext(Set<ClassFile> msClassFiles, SeerSecurityContext securityContext) {

        ClassPool cp = ClassPool.getDefault();
        List<CtClass> classes = new ArrayList<>();

        for (ClassFile classFile : msClassFiles) {

            CtClass clazz = null;
            try {
                clazz = cp.makeClass(classFile);
                classes.add(clazz);
            } catch (Exception e) {
                System.out.println("Failed to make class:" + e.toString());
                break;
            }
        }

        SecurityFilterContext securityFilterContext =
                new SecurityFilterContext(new SecurityFilterGeneralAnnotationStrategy());

        Set<SecurityMethod> methods = new HashSet<>();

        for ( CtClass clazz : classes ) {
            securityFilterContext.doFilter(clazz, methods);
        }

        securityContext.setSecurityMethods(methods);

        return securityContext;
    }

}
