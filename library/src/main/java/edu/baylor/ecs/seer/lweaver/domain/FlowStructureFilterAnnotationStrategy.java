package edu.baylor.ecs.seer.lweaver.domain;

import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

public class FlowStructureFilterAnnotationStrategy implements FlowStructureFilterStrategy {

    @Override
    public boolean doFilter(CtClass clazz) {
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) clazz.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        if(annotationsAttribute != null) {
            Annotation[] annotations = annotationsAttribute.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.getTypeName().equals("org.springframework.stereotype.Service") || annotation.getTypeName().equals("org.springframework.stereotype.Component")){
                    return true;
                }
            }
        }
        return false;
    }

}
