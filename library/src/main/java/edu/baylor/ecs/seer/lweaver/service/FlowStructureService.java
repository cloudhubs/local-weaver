package edu.baylor.ecs.seer.lweaver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.common.context.SeerFlowContext;
import edu.baylor.ecs.seer.common.flow.SeerFlowMethod;
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
public class FlowStructureService {

    protected final SeerFlowContext process(List<CtClass> classes, SeerContext context){
        Map<SeerFlowMethod, List<SeerFlowMethod>> seerFlowMethods = new HashMap<>();
//        Map<List<String>, List<List<String>>> formattedMap = new HashMap<>();

        // Loop through every class in the array
        for(CtClass clazz : classes){

            // Retrieve all the methods of a class
            CtMethod[] methods = clazz.getDeclaredMethods();

            // Loop through every method
            for(CtMethod method : methods){

                // Build the key for the formattedMap
                SeerFlowMethod seerFlowMethod = new SeerFlowMethod();
                seerFlowMethod.setClassName(clazz.getName());
                seerFlowMethod.setMethodName(method.getName());

                // Add the formattedKey to the formattedMap
                seerFlowMethods.put(seerFlowMethod, new ArrayList<>());


                // Instrument the method to pull out the method calls
                try {
                    method.instrument(
                            new ExprEditor() {
                                public void edit(MethodCall m) {

                                    // Retrieve the list of subMethods
                                    List<SeerFlowMethod> subMethodList = seerFlowMethods.get(seerFlowMethod);

                                    // Build the key for the subMethod
                                    SeerFlowMethod subMethodKey = new SeerFlowMethod();
                                    subMethodKey.setClassName(m.getClassName());
                                    subMethodKey.setMethodName(m.getMethodName());
                                    subMethodList.add(subMethodKey);
                                }
                            }
                    );
                } catch (CannotCompileException e){
                    System.out.println(e.toString());
                }
            }
        }

        SeerFlowContext seerFlowContext = new SeerFlowContext();
        seerFlowContext.setSeerFlowMethods(seerFlowMethods);
        return seerFlowContext;

//        try {
//            applicationStructureInJson = new ObjectMapper().writeValueAsString(formattedMap);
//        } catch (Exception e){
//            System.out.println(e.toString());
//        }

//        return context; // temporary, must implement use of context
        // deprecated
        //return applicationStructureInJson;
    }

    protected final boolean filter(CtClass clazz){

        FlowStructureFilterContext filter =
                new FlowStructureFilterContext(new FlowStructureFilterNameStrategy());

        return filter.doFilter(clazz);

    }

}
