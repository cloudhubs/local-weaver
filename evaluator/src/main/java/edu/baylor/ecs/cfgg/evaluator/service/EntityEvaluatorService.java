package edu.baylor.ecs.cfgg.evaluator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.CtClass;
import javassist.CtField;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EntityEvaluatorService extends EvaluatorService {

    protected final String process(List<CtClass> entities) {

        Map<String, Map<String, List< List<String> > >> map = new HashMap<>();

        // Loop through every class
        for(CtClass clazz : entities){

            // Create map for storing fields and their annotations
            Map<String, List< List<String> >> fieldMap = new HashMap<>();

            // Get the fields and loop through them
            CtField[] fields = clazz.getFields();
            CtField[] privateFields = clazz.getDeclaredFields();

            List<CtField> aggregateFields = new ArrayList<>();
            aggregateFields.addAll(Arrays.asList(fields));
            aggregateFields.addAll(Arrays.asList(privateFields));

            for(CtField field : aggregateFields) {

                // Add the field to the map
                fieldMap.put(field.getName(), new ArrayList<>());

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

                                // Build the pair
                                List<String> key = new ArrayList<>();
                                key.add(name);
                                key.add(value.toString());

                                // Add key to fieldMap
                                fieldMap.get(field.getName()).add(key);
                            }
                        }

                    }
                }
            }

            map.put(clazz.getName(), fieldMap);

        }


        String inJson = "";
        try {
            inJson = new ObjectMapper().writeValueAsString(map);
        } catch (Exception e){
            System.out.println(e.toString());
        }

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
