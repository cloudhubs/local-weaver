package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.context.SeerEntityContext;
import edu.baylor.ecs.seer.common.entity.EntityModel;
import edu.baylor.ecs.seer.common.entity.SeerEntityRelation;
import edu.baylor.ecs.seer.common.entity.SeerField;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * The SeerMsEntityContextService service constructs a
 * {@link edu.baylor.ecs.seer.common.context.SeerEntityContext} from an initial list of
 * {@link javassist.CtClass}
 *
 * </p>
 *
 * The entry point method is {@link SeerMsEntityContextService#getSeerEntityContext(List)}
 * which filters the initial list of classes using the
 * {@link SeerMsEntityContextService#getEntityClasses(List)}
 * method and constructs the {@link edu.baylor.ecs.seer.common.context.SeerEntityContext} object.
 *
 * </p>
 *
 * The {@link SeerMsEntityContextService#getEntityClasses(List)} method filters out
 * any class that doesn't contain {@link javax.persistence.Entity} annotation.
 *
 * </p>
 *
 * The actual {@link edu.baylor.ecs.seer.common.context.SeerEntityContext} object is constructed in the
 * {@link SeerMsEntityContextService#deriveEntities(List)} method. This method loops over the
 * filtered list of {@link javassist.CtClass} entities to convert them into
 * {@link edu.baylor.ecs.seer.common.entity.SeerField} objects. The list of
 * {@link edu.baylor.ecs.seer.common.entity.SeerField} objects are used to construct
 * a {@link edu.baylor.ecs.seer.common.context.SeerEntityContext} which is returned.
 *
 * @author  Jan Svacina
 * @version 2.0
 * @since   0.3.0
 */
@Service
public class SeerMsEntityContextService {

    /**
     * Returns a {@link edu.baylor.ecs.seer.common.context.SeerEntityContext} that represents the
     * entities for the particular microservice. This is the entry method for
     * {@link edu.baylor.ecs.seer.lweaver.service.SeerMsEntityContextService}
     *
     * @param  allClasses  a {@link java.util.List} of {@link javassist.CtClass} object
     * that represents all the possible classes in the microservice
     *
     * @return the {@link SeerEntityContext} representing the entities in the microservice
     * @see {@link edu.baylor.ecs.seer.lweaver.service.SeerContextService#generateMsContexts(List, String)}
     */
    SeerEntityContext getSeerEntityContext(List<CtClass> allClasses){

        /*
         * ToDo: This method disappear since it entities will be given to the method
         */

        List<CtClass> entityClasses = getEntityClasses(allClasses);
        return deriveEntities(entityClasses);
    }

    /**
     * Returns a {@link java.util.List} of {@link javassist.CtClass} objects that have the
     * {@link javax.persistence.Entity} annotation. This is a private helper method called from
     * {@link SeerMsEntityContextService#getSeerEntityContext(List)}
     *
     * @param  allClasses  a {@link java.util.List} of {@link javassist.CtClass} object
     * that represents all the possible classes in the microservice
     *
     * @return a {@link java.util.List} of {@link CtClass} objects that have the
     * {@link javax.persistence.Entity} annotation
     *
     * @see {@link edu.baylor.ecs.seer.lweaver.service.SeerContextService#generateMsContexts(List, String)}
     */
    private List<CtClass> getEntityClasses(List<CtClass> allClasses){

        /*
         * ToDo: Repeated operations for all concerns (entity, service, repository, controller)
         */

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
     * This method returns a {@link SeerEntityContext} built on the classes annotated
     * with {@link javax.persistence.Entity}. This is a private helper method called from
     * {@link SeerMsEntityContextService#getSeerEntityContext(List)}
     *
     * @param  entityClasses  a {@link java.util.List} of {@link javassist.CtClass} object
     * that hold all {@link javassist.CtClass} objects annotated with the
     * {@link javax.persistence.Entity} annotation
     *
     * @return a {@link SeerEntityContext} representing the {@link javax.persistence.Entity}
     * classes from the microservice
     *
     * @see {@link edu.baylor.ecs.seer.lweaver.service.SeerContextService#generateMsContexts(List, String)}
     */
    private SeerEntityContext deriveEntities(List<CtClass> entityClasses){

        /*
         * ToDo: Different strategy for annotations on fields and setters (FieldAnnotationStrategy...)
         * ToDo: @Column does not have to be necessary included!
         * ToDo: separate building the object, field aggregation, field processing, find matching setter
         */

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
            Set<CtField> aggregateFields = new HashSet<>();
            aggregateFields.addAll(Arrays.asList(fields));
            aggregateFields.addAll(Arrays.asList(privateFields));

            List<SeerField> seerFields = new ArrayList<>();

            // Loop through all of the instance fields
            for(CtField field : aggregateFields) {

                // SeerField init
                SeerField seerField = new SeerField();
                seerField.setName(field.getName());

                try {
                    String fullType = field.getType().getName();
                    String type = fullType.substring(fullType.lastIndexOf('.') + 1);

                    // We don't want a relationship with List or Set objects
                    if (type.equals("List") || type.equals("Set")){
                        continue;
                    }

                    seerField.setFullType(fullType);
                    seerField.setType(type);


                } catch (NotFoundException e) {
                    e.printStackTrace();
                }

                // Get the attributes and loop through them
                AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) field.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);
                if (annotationsAttribute != null) {

                    Annotation[] annotations = annotationsAttribute.getAnnotations();

                    // Loop through annotations on field
                    for (Annotation annotation : annotations) {

                        // ToDo: Refactor this switch statement to be more elegant
                        // https://stackoverflow.com/questions/126409/ways-to-eliminate-switch-in-code

                        String annotationType = annotation.getTypeName();
                        switch (annotationType){
                            case ("javax.persistence.ManyToOne"):
                                seerField.setSeerEntityRelation(SeerEntityRelation.MANYTOONE);
                                break;
                            case ("javax.persistence.OneToMany"):
                                seerField.setSeerEntityRelation(SeerEntityRelation.ONETOMANY);
                                break;
                            case ("javax.validation.constraints.NotNull"):
                                seerField.setNotNull(true);
                                break;
                            case ("javax.validation.constraints.Size"):
                                MemberValue max = annotation.getMemberValue("max");
                                if (max != null){
                                    seerField.setMax(Integer.parseInt(max.toString()));
                                }
                                MemberValue min = annotation.getMemberValue("min");
                                if (min != null){
                                    seerField.setMin(Integer.parseInt(min.toString()));
                                }
                                break;
                            default:
                                // default empty on purpose
                        }

                    }
                }
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
