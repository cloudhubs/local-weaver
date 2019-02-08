package edu.baylor.ecs.seer.lweaver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.baylor.ecs.seer.common.context.SeerContext;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates flow
 */
@Service
public class FlowStructureService extends EvaluatorService {

    protected final SeerContext process(List<CtClass> classes, SeerContext context){
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

        return context; // temporary, must implement use of context
        // deprecated
        //return applicationStructureInJson;
    }

    protected final boolean filter(CtClass clazz){

        FlowStructureFilterContext filter =
                new FlowStructureFilterContext(new FlowStructureFilterNameStrategy());

        return filter.doFilter(clazz);

    }

}
