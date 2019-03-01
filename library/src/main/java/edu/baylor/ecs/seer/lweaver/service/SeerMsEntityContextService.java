package edu.baylor.ecs.seer.lweaver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.baylor.ecs.seer.common.context.SeerEntityContext;
import edu.baylor.ecs.seer.common.entity.EntityModel;
import edu.baylor.ecs.seer.common.entity.InstanceVariableModel;
import edu.baylor.ecs.seer.common.entity.SeerEntityRelation;
import edu.baylor.ecs.seer.common.entity.SeerField;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Generates Seer Entity Context
 */
@Service
public class SeerMsEntityContextService {


    /**
     * Main method: filters entity methods
     * ToDo: This method disappear since it entities will be given to the method
     * @param allClasses
     * @return
     */
    public SeerEntityContext getSeerEntityContext(List<CtClass> allClasses){

        List<CtClass> entityClasses = getEntityClasses(allClasses);

        SeerEntityContext seerEntityContext = deriveEntities(entityClasses);

        return seerEntityContext;
    }

    /**
     * Get only those ct classes that are entity objects
     * ToDo: Repeated operations for all concerns (entity, service, repository, controller)
     * @param allClasses
     * @return
     */
    private List<CtClass> getEntityClasses(List<CtClass> allClasses){
        List<CtClass> entityClasses = new ArrayList<>();
        for (CtClass ctClass: allClasses
        ) {
            AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
            if(annotationsAttribute != null) {
                Annotation[] annotations = annotationsAttribute.getAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation.getTypeName().equals("javax.persistence.Entity")) {
                        entityClasses.add(ctClass);
                    }
                }
            }
        }
        return entityClasses;
    }

    /**
     * Iterates entity classes and derives field names, annotations, etc.
     * ToDo: Different strategy for annotations on fields and setters (FieldAnnotationStrategy...)
     * ToDo: @Column does not have to be necessary included!
     * ToDo: separate building the object, field aggregation, field processing, find matching setter
     * @param entityClasses
     * @return
     */
    public SeerEntityContext deriveEntities(List<CtClass> entityClasses){

        // Establish the list of entities
        List<EntityModel> entities = new ArrayList<>();

        // Loop through every class
        for(CtClass clazz : entityClasses){

            // Create a new EntityModel for the class
            EntityModel entityModel = new EntityModel(clazz.getName());
            entityModel.setClassNameShort(clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1));


            // Get all the public and private fields
            CtField[] fields = clazz.getFields();
            CtField[] privateFields = clazz.getDeclaredFields();
            List<CtField> aggregateFields = new ArrayList<>();
            //aggregateFields.addAll(Arrays.asList(fields));
            aggregateFields.addAll(Arrays.asList(privateFields));

            List<SeerField> seerFields = new ArrayList<>();

            // Loop through all of the instance fields
            for(CtField field : aggregateFields) {

                // Create a model for the instance field
                InstanceVariableModel instanceVariableModel = new InstanceVariableModel(field.getName());
                // SeerField init
                SeerField seerField = new SeerField();
                seerField.setName(field.getName());
                try {
                    String rawName = field.getType().getName();
                    seerField.setFullType(rawName);
                    seerField.setType(rawName.substring(rawName.lastIndexOf('.') + 1));
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }

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
                        //ToDo: Massive ugly decision switch
                        //https://stackoverflow.com/questions/126409/ways-to-eliminate-switch-in-code
                        System.out.println(annotation.getTypeName());
                        String annotationType = annotation.getTypeName();
                        switch (annotationType){
                            case ("javax.persistence.ManyToOne"):
                                seerField.setSeerEntityRelation(SeerEntityRelation.MANYTOONE);
                                break;
                            case ("javax.persistance.OneToMany"):
                                seerField.setSeerEntityRelation(SeerEntityRelation.ONETOMANY);
                                break;
                            //case ()
                            case ("javax.validation.constraints.NotNull"):
                                seerField.setNotNull(true);
                                break;
                            case ("javax.validation.constraints.Size"):
                                MemberValue max = annotation.getMemberValue("max");
                                if (max != null){
                                    seerField.setMax(new Integer(max.toString()));
                                }
                                MemberValue min = annotation.getMemberValue("min");
                                if (min != null){
                                    seerField.setMin(new Integer(min.toString()));
                                }
                                break;
                            default:
                                    //
                        }

                    }
                }
                // Add the field to the entity
                entityModel.addInstanceVariableModel(instanceVariableModel);
                //
                seerFields.add(seerField);
            }
            entityModel.setFields(seerFields);
            // Add the entity to the list
            entities.add(entityModel);
        }

        // Return the list of entities as JSON
        SeerEntityContext seerEntityContext = new SeerEntityContext();
        seerEntityContext.setEntities(entities);
        seerEntityContext.setEntityCounter(entities.size());
        return seerEntityContext;
    }
}
