package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.entity.SeerCtClassWrapper;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SeerClassBuilderService {

    /**
     * Get Seer Classes
     * @param allClasses
     * @return
     */
    public SeerCtClassWrapper getSeerClasses(List<CtClass> allClasses){

        SeerCtClassWrapper ctClassWrapper = new SeerCtClassWrapper();
        for (CtClass ctClass: allClasses
        ) {
            AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
            if(annotationsAttribute != null) {
                Annotation[] annotations = annotationsAttribute.getAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation.getTypeName().equals("javax.persistence.Entity")) {
                        ctClassWrapper.addServiceClass(ctClass);
                    }
                    // Api Out classes
                    if (annotation.getTypeName().equals("javax.ws.rs.Path")){
                        ctClassWrapper.addApiOutCtClass(ctClass);
                    }
                    // Service classes
                    if (annotation.getTypeName().equals("javax.ws.rs.Path")
                            || annotation.getTypeName().equals("javax.enterprise.context.RequestScoped")
                            || annotation.getTypeName().equals("org.springframework.stereotype.Service")
                            || annotation.getTypeName().equals("org.springframework.stereotype.Component")
                    ){
                        ctClassWrapper.addServiceClass(ctClass);
                    }
                }
            }
        }
        return ctClassWrapper;
    }

}
