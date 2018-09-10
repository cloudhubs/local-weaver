package edu.baylor.ecs.cfgg.evaluator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.baylor.ecs.cfgg.evaluator.repository.EvaluatorRepository;
import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Mnemonic;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EvaluatorService {

    @Autowired
    private EvaluatorRepository evaluatorRepository;

    private Map<List<String>, List<List<String>>> formattedMap;

    public String deriveApplicationStructure(){

        // Setup some initial objects
        formattedMap = new HashMap<>();
        String applicationStructureInJson = "";
        ClassPool cp = ClassPool.getDefault();

        // Loop through every class in the array
        for(Class className : evaluatorRepository.getClasses()){

            // Try to get the class as a CtClass
            CtClass clazz = null;
            try {
                clazz = cp.get(className.getName());
            } catch (Exception e){
                System.out.println(e.toString());
                break;
            }

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
        // Build the JSON and return it
        return applicationStructureInJson;
    }
}
