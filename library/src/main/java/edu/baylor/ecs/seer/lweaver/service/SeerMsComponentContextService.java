package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.components.ComponentModel;
import edu.baylor.ecs.seer.common.components.ComponentType;
import edu.baylor.ecs.seer.common.context.SeerComponentsContext;
import edu.baylor.ecs.seer.common.entity.SeerEntityRelation;
import edu.baylor.ecs.seer.common.entity.SeerField;
import edu.baylor.ecs.seer.common.entity.SeerFlowMethodRepresentation;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SeerMsComponentContextService {

    // Service for managing the flow aspect of the microservices
    private final FlowStructureService flowService;

    // Service for managing the bytecode flow aspect of the microservices
    private final BytecodeFlowStructureService bytecodeService;

    public SeerMsComponentContextService(FlowStructureService flowService, BytecodeFlowStructureService bytecodeFlowStructureService) {
        this.flowService = flowService;
        this.bytecodeService =  bytecodeFlowStructureService;
    }

    public SeerComponentsContext getComponentClasses(List<CtClass> allClasses){

        /*
         * ToDo: Repeated operations for all concerns (entity, service, repository, controller)
         */
        SeerComponentsContext seerComponents = new SeerComponentsContext();

        Set<CtClass> entityClasses = new HashSet<>();
        Set<CtClass> controllerClasses = new HashSet<>();
        Set<CtClass> serviceClasses = new HashSet<>();
        Set<CtClass> repositoryClasses = new HashSet<>();
        Set<CtClass> genericClasses = new HashSet<>();

        for (CtClass ctClass: allClasses) {
            AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
            if(annotationsAttribute != null) {
                Annotation[] annotations = annotationsAttribute.getAnnotations();
                for (Annotation annotation : annotations) {
                    ComponentType.Type componentType = ComponentType.getComponentType(annotation.getTypeName());
                    if(!(componentType == null)){
                        switch (componentType) {
                            case ENTITY:
                                entityClasses.add(ctClass);
                                break;
                            case CONTROLLER:
                                controllerClasses.add(ctClass);
                                break;
                            case SERVICE:
                                serviceClasses.add(ctClass);
                                break;
                            case REPOSITORY:
                                repositoryClasses.add(ctClass);
                                break;
                            default:
                                genericClasses.add(ctClass);
                                break;
                        }
                    }
                }
            }
        }

        seerComponents.setEntities(deriveComponent(entityClasses, ComponentType.Type.ENTITY));
        seerComponents.setControllers(deriveComponent(controllerClasses, ComponentType.Type.CONTROLLER));
        seerComponents.setServices(deriveComponent(serviceClasses, ComponentType.Type.SERVICE));
        seerComponents.setComponents(deriveComponent(genericClasses, ComponentType.Type.GENERIC_COMPONENT));
        seerComponents.setRepositories(deriveComponent(repositoryClasses, ComponentType.Type.REPOSITORY));

        return seerComponents;
    }

    private List<ComponentModel> deriveComponent(Set<CtClass> controllerClasses, ComponentType.Type componentType){

        /*
         * ToDo: Different strategy for annotations on fields and setters (FieldAnnotationStrategy...)
         * ToDo: @Column does not have to be necessary included!
         * ToDo: separate building the object, field aggregation, field processing, find matching setter
         */

        List<ComponentModel> components = new ArrayList<>();

        // Loop through every class
        for(CtClass clazz : controllerClasses){

            // Create a new EntityModel for the class
            ComponentModel component = new ComponentModel(clazz.getName());
            component.setClassNameShort(clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1));


            component.setFields(setFields(clazz, componentType));
            component.setMethods(setMethods(clazz, componentType));

            // Add the entity to the list
            components.add(component);
        }

        return components;
    }

    private List<SeerField> setFields(CtClass clazz, ComponentType.Type componentType) {
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

            if(componentType == ComponentType.Type.ENTITY) {
                applyEntityOptions(field, seerField);
            }

            seerFields.add(seerField);

        }
        return seerFields;
    }

    private List<SeerFlowMethodRepresentation> setMethods(CtClass clazz, ComponentType.Type componentType) {
        List<SeerFlowMethodRepresentation> methodRepresentations = flowService.processClazz(clazz);
        bytecodeService.process(methodRepresentations);
        return methodRepresentations;
    }

    private void applyEntityOptions(CtField field, SeerField seerField) {
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
    }
}
