package edu.baylor.ecs.cfgg.evaluator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.baylor.ecs.cfgg.evaluator.repository.EvaluatorRepository;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EvaluatorService {

    @Autowired
    private EvaluatorRepository evaluatorRepository;

    private ClassPathScanner classPathScanner = new ClassPathScanner();
    private Set<ClassFile> classFileSet;

    public String deriveApplicationStructure(){

        ClassPool cp = ClassPool.getDefault();

        classPathScanner.scanUri("file://///Users/walkerand/Documents/Research/sm-core-2.2.0-SNAPSHOT.jar");
        classFileSet = classPathScanner.getClasses();
        classPathScanner.scanUri("file://///Users/walkerand/Documents/Research/sm-core-model-2.2.0-SNAPSHOT.jar");
        classFileSet.addAll(classPathScanner.getClasses());
        classPathScanner.scanUri("file://///Users/walkerand/Documents/Research/sm-core-modules-2.2.0-SNAPSHOT.jar");
        classFileSet.addAll(classPathScanner.getClasses());

        List<CtClass> componentsAndServices = new ArrayList<>();
        List<CtClass> entities = new ArrayList<>();

        for(ClassFile classFile : classFileSet){

            CtClass clazz = null;
            try {
                clazz = cp.makeClass(classFile);
            } catch (Exception e){
                System.out.println(e.toString());
                break;
            }

            AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
            if(annotationsAttribute != null) {
                Annotation[] annotations = annotationsAttribute.getAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation.getTypeName().equals("org.springframework.stereotype.Service") || annotation.getTypeName().equals("org.springframework.stereotype.Component")){
                        componentsAndServices.add(clazz);
                    } else if (annotation.getTypeName().equals("javax.persistence.Entity")) {
                        entities.add(clazz);
                    }
                }
            }
        }

        return generateMap(componentsAndServices);
    }

    private String generateMap(List<CtClass> classes){
        // Setup some initial objects
        Map<List<String>, List<List<String>>> formattedMap = new HashMap<>();
        String applicationStructureInJson = "";

        // Loop through every class in the array
        for(CtClass clazz : classes){

            // Retrieve all the methods of a class
            CtMethod[] methods = clazz.getDeclaredMethods();

            // Loop through every method
            for(CtMethod method : methods){

                // Build the key for the formattedMap
                ArrayList<String> formattedKey = new ArrayList<>();
                formattedKey.add(clazz.getName());
                formattedKey.add(method.getName());

                // Add the formattedKey to the formattedMap
                formattedMap.put(formattedKey, new ArrayList<>());


                // Instrument the method to pull out the method calls
                try {
                    method.instrument(
                        new ExprEditor() {
                            public void edit(MethodCall m) {

                                // Retrieve the list of subMethods
                                List<List<String>> subMethodList = formattedMap.get(formattedKey);

                                // Build the key for the subMethod
                                ArrayList<String> subMethodKey = new ArrayList<>();
                                subMethodKey.add(m.getClassName());
                                subMethodKey.add(m.getMethodName());

                                subMethodList.add(subMethodKey);
                            }
                        }
                    );
                } catch (CannotCompileException e){
                    System.out.println(e.toString());
                }
            }
        }

        try {
            applicationStructureInJson = new ObjectMapper().writeValueAsString(formattedMap);
        } catch (Exception e){
            System.out.println(e.toString());
        }

        return applicationStructureInJson;
    }
}
