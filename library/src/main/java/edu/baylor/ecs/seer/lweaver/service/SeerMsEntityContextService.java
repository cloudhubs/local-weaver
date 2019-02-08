package edu.baylor.ecs.seer.lweaver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.baylor.ecs.seer.common.context.SeerEntityContext;
import edu.baylor.ecs.seer.common.entity.EntityModel;
import edu.baylor.ecs.seer.common.entity.InstanceVariableModel;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;



@Service
public class SeerMsEntityContextService {

    public SeerEntityContext getSeerEntityContext(Set<ClassFile> msClassFiles){
        // Establish the list of entities
        List<EntityModel> entities = new ArrayList<>();

        List<CtClass> entityClasses = convertClassFileSetToCtClasses(msClassFiles);

        // Loop through every class
        for(CtClass clazz : entityClasses){

            // Create a new EntityModel for the class
            EntityModel entityModel = new EntityModel(clazz.getName());

            // Get all the public and private fields
            CtField[] fields = clazz.getFields();
            CtField[] privateFields = clazz.getDeclaredFields();
            List<CtField> aggregateFields = new ArrayList<>();
            aggregateFields.addAll(Arrays.asList(fields));
            aggregateFields.addAll(Arrays.asList(privateFields));

            // Loop through all of the instance fields
            for(CtField field : aggregateFields) {

                // Create a model for the instance field
                InstanceVariableModel instanceVariableModel = new InstanceVariableModel(field.getName());

                // Get the attributes and loop through them
                AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) field.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);
                if (annotationsAttribute != null) {
                    Annotation[] annotations = annotationsAttribute.getAnnotations();

                    // Loop through annotations on field
                    for (Annotation annotation : annotations) {

                        // Only process annotations with @Column or @JoinColumn
                        if (annotation.getTypeName().equals("javax.persistence.JoinColumn") || annotation.getTypeName().equals("javax.persistence.Column")) {
                            // Get the names and loop
                            Set<String> names = annotation.getMemberNames();
                            for (String name : names) {
                                // Get the value for the name
                                MemberValue value = annotation.getMemberValue(name);

                                // Add the attirbute to the instance field model
                                instanceVariableModel.addAttribute(name, value.toString());
                            }
                        }

                    }
                }

                // Add the field to the entity
                entityModel.addInstanceVariableModel(instanceVariableModel);
            }

            // Add the entity to the list
            entities.add(entityModel);
        }

        SeerEntityContext seerEntityContext = new SeerEntityContext();
        seerEntityContext.setEntities(entities);

        return seerEntityContext;
    }

    protected final boolean isCtClassEntityClass(CtClass clazz){
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) clazz.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        if(annotationsAttribute != null) {
            Annotation[] annotations = annotationsAttribute.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.getTypeName().equals("javax.persistence.Entity")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * Converts classFileSetToCtClasses
     * @param classFileSet
     * @return
     */
    private List<CtClass> convertClassFileSetToCtClasses(Set<ClassFile> classFileSet){
        List<CtClass> classes = new ArrayList<>();
        ClassPool cp = ClassPool.getDefault();
        for(ClassFile classFile : classFileSet){
            CtClass ctClass = null;
            try {
                ctClass = cp.makeClass(classFile);
            } catch (Exception e){
                System.out.println("Failed to make class:" + e.toString());
                break;
            }
            if(isCtClassEntityClass(ctClass)){
                classes.add(ctClass);
            }
        }
        return classes;
    }
}
