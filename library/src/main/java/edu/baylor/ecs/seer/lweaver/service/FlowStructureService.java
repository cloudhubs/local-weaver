package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.common.context.SeerFlowContext;
import edu.baylor.ecs.seer.common.entity.SeerFlowMethodRepresentation;
import edu.baylor.ecs.seer.common.flow.SeerFlowMethod;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates flow
 */
@Service
public class FlowStructureService {

    public SeerFlowContext process(List<CtClass> classes){
        List<SeerFlowMethodRepresentation> methodRepresentations = new ArrayList<>();

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

                SeerFlowMethodRepresentation representation = new SeerFlowMethodRepresentation(clazz.getName(), method.getName(), new ArrayList<>());

                // Instrument the method to pull out the method calls
                try {
                    method.instrument(
                        new ExprEditor() {
                            public void edit(MethodCall m) {

                                // Retrieve the list of subMethods
                                List<SeerFlowMethod> subMethodList = representation.getChildren();

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

                methodRepresentations.add(representation);

            }
        }

        SeerFlowContext seerFlowContext = new SeerFlowContext();
        seerFlowContext.setSeerFlowMethods(methodRepresentations);
        return seerFlowContext;
    }
}
