package edu.baylor.ecs.cfgg.evaluator.service;

import edu.baylor.ecs.cfgg.evaluator.repository.LoaderRepository;
import javassist.*;
import javassist.bytecode.MethodInfo;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EvaluatorService {

    @Autowired
    private LoaderRepository loaderRepository;

    private Map<String, ArrayList<String>> map;

    public String deriveApplicationStructure(){
        String classes = loaderRepository.getClasses();
        map = new HashMap<>();
        String applicationStructureInJson = "";

        String[] classArr = classes.split(":");

        ClassPool cp = ClassPool.getDefault();
        for(String className : classArr){
            CtClass clazz = null;
            try {
                clazz = cp.get(className);
            } catch (Exception e){
                System.out.println(e.toString());
                break;
            }
            // Retrieve all the methods of a class
            CtMethod[] methods = clazz.getDeclaredMethods();

            // Loop through every method
            for(CtMethod method : methods){

                // Store the className and methodName for building the key within the inner class
                String innerClassName = clazz.getName();
                String innerMethodName = method.getName();

                // Build the key for this method
                String key = getKey(innerClassName, innerMethodName);

                // If this class:method isn't in the map, store it with a new list
                if(!map.containsKey(key)){
                    map.put(key, new ArrayList<>());
                }

                try {
                    // Instrument the method to pull out the method calls
                    method.instrument(
                        new ExprEditor() {
                            public void edit(MethodCall m) {

                                // Store the inner method call within the map
                                ArrayList<String> list = map.get(key);
                                list.add(getKey(m.getClassName(), m.getMethodName()));

                                System.out.println(getKey(m.getClassName(), m.getMethodName()));
                            }
                        }
                    );
                } catch (CannotCompileException e){
                    System.out.println(e.toString());
                }
            }
        }

        return applicationStructureInJson;
    }

    private String getKey(String className, String methodName){
        return className.concat(":").concat(methodName);
    }

}
