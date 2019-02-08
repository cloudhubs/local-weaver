package edu.baylor.ecs.seer.lweaver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.baylor.ecs.seer.common.entity.EntityModel;
import edu.baylor.ecs.seer.common.entity.InstanceVariableModel;
import javassist.CtClass;
import javassist.CtField;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;
import org.springframework.stereotype.Service;

import java.util.*;

/** Deprecated **/
@Service
public class DataModelService {

    public String process(List<CtClass> entityClasses) {

        // Establish the list of entities
        List<EntityModel> entities = new ArrayList<>();

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

        // Convert the list of entities into JSON
        String inJson = "";
        try {
            inJson = new ObjectMapper().writeValueAsString(entities);
        } catch (Exception e){
            System.out.println(e.toString());
        }

        // Return the list of entities as JSON
        return inJson;
    }

    protected final boolean filter(CtClass clazz){
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
}
