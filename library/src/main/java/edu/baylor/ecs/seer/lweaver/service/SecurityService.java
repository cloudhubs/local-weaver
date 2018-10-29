package edu.baylor.ecs.seer.lweaver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SecurityService extends EvaluatorService {

    private Map<String, List<CtClass>> roles = new HashMap<>();

    protected final String process(List<CtClass> classes){
        // Setup the return string
        String applicationStructureInJson = "";

        // Loop through all the roles
        for(String key : roles.keySet()) {

            // Setup some initial objects
            Map<List<String>, List<List<String>>> formattedMap = new HashMap<>();

            // Loop through every class the role has access to
            for (CtClass clazz : roles.get(key)) {

                // Retrieve all the methods of a class
                CtMethod[] methods = clazz.getDeclaredMethods();

                // Loop through every method
                for (CtMethod method : methods) {

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
                    } catch (CannotCompileException e) {
                        System.out.println(e.toString());
                    }
                }
            }

            try {
                String processedJSON = new ObjectMapper().writeValueAsString(formattedMap);
                applicationStructureInJson = applicationStructureInJson.concat(key + "\n");
                applicationStructureInJson = applicationStructureInJson.concat(processedJSON + "\n");
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }

        return applicationStructureInJson;
    }

    // This filter will be slightly different than the others in that we want to break up the sorting into multiple
    // lists of CtClass objects by role. This filter function will be used to cache the classes in a private map
    // which will then be used in process
    protected final boolean filter(CtClass clazz){
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) clazz.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        if(annotationsAttribute != null) {
            Annotation[] annotations = annotationsAttribute.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.getTypeName().equals("javax.annotation.security.RolesAllowed")) {

                    // Get the names and loop
                    Set<String> names = annotation.getMemberNames();
                    for (String name : names) {
                        // Get the value for the name
                        MemberValue value = annotation.getMemberValue(name);

                        if(value instanceof ArrayMemberValue){
                            ArrayMemberValue amv = (ArrayMemberValue)value;
                            MemberValue[] memberValues = amv.getValue();
                            for(MemberValue mv : memberValues){
                                String key = mv.toString().replace("\"", "");
                                if(roles.get(key) == null) {
                                    roles.put(key, new ArrayList<>());
                                }
                                roles.get(key).add(clazz);
                            }
                        }
                    }
                    return true;
                } else if (annotation.getTypeName().equals("javax.annotation.security.PermitAll")){
                    Iterator it = roles.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String)it.next();
                        List<CtClass> list = roles.get(key);
                        list.add(clazz);
                    }
                    return true;

                }
            }
        }
        return false;
    }
}
