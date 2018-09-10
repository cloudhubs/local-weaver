package edu.baylor.ecs.cfgg.evaluator.service;

import edu.baylor.ecs.cfgg.evaluator.repository.EvaluatorRepository;
import edu.baylor.ecs.cfgg.evaluator.service.models.MethodModel;
import javafx.util.Pair;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EvaluatorService {

    @Autowired
    private EvaluatorRepository evaluatorRepository;

    private Map<String, ArrayList<MethodModel>> map;

    public String deriveApplicationStructure(){

        // Setup some initial objects
        String classes = evaluatorRepository.getClasses();
        map = new HashMap<>();
        String applicationStructureInJson;
        String[] classArr = classes.split(":");
        ClassPool cp = ClassPool.getDefault();

        // Loop through every class in the array
        for(String className : classArr){

            // Try to get the class as a CtClass
            CtClass clazz = null;
            try {
                clazz = cp.get(className);
            } catch (Exception e){
                System.out.println(e.toString());
                break;
            }

            // Store the key for the calling class
            String key = clazz.toString();

            // If this class isn't in the map, store it with a new list
            if(!map.containsKey(key)){
                map.put(key, new ArrayList<>());
            }

            // Retrieve all the methods of a class
            CtMethod[] methods = clazz.getDeclaredMethods();

            // Loop through every method
            for(CtMethod method : methods){

                // Retrieve the list of all method mappings from the class
                ArrayList<MethodModel> list = map.get(key);

                // Loop through and check each of the MethodModels to see if it corresponds to the current method
                boolean found = false;
                for(MethodModel methodModel : list){
                    if(methodModel.getMethod().equals(method)){
                        // If the method model is for this method then no need to add it
                        found = true;
                    }
                }

                // If there isnt a MethodModel for this method, add it
                if(!found){
                    MethodModel methodModel = new MethodModel(method);
                    list.add(methodModel);
                }

                // Instrument the method to pull out the method calls
                try {
                    method.instrument(
                        new ExprEditor() {
                            public void edit(MethodCall m) {

                                for(MethodModel methodModel : list){
                                    if(methodModel.getMethod().equals(method)){
                                        // If the method model is for this method then add the sub method
                                        methodModel.addSubMethod(m.getClassName(), m.getMethodName());
                                    }
                                }
                            }
                        }
                    );
                } catch (CannotCompileException e){
                    System.out.println(e.toString());
                }
            }
        }

        // Build the JSON and return it
        applicationStructureInJson = buildJson(map);
        return applicationStructureInJson;
    }

    private String buildJson(Map<String, ArrayList<MethodModel>> map) {
        String JSON = "";

        Iterator<String> it = map.keySet().iterator();
        while (it.hasNext()) {
            String className = it.next();
            JSON = JSON.concat("\"").concat(className).concat("\" { \n");
            List<MethodModel> methodModelList = map.get(className);
            for(MethodModel methodModel : methodModelList){
                JSON = JSON.concat("\t\"").concat(methodModel.getMethod().getName()).concat("\" : { \n");

                for(Pair<String, String> subMethod : methodModel.getSubMethods()){
                    JSON = JSON.concat("\t\t\"").concat(subMethod.getKey()).concat("\":\"").concat(subMethod.getValue()).concat("\"\n");
                }

                JSON = JSON.concat("\t} \n");
            }

            JSON = JSON.concat("} \n");
        }

        return JSON;
    }
}
