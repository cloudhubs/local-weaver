package edu.baylor.ecs.seer.lweaver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SecurityService extends EvaluatorService {

    private Map<String, List<String>> roles = new HashMap<>();
    private Map<String, List<String>> nodes = new HashMap<>();

    protected final String process(List<CtClass> classes){
        // Setup the return string
        String applicationStructureInJson = "";

        try {
            applicationStructureInJson = new ObjectMapper().writeValueAsString(roles);
            applicationStructureInJson = applicationStructureInJson.concat("\n@");
            String s = new ObjectMapper().writeValueAsString(nodes);
            applicationStructureInJson = applicationStructureInJson.concat(s);
            applicationStructureInJson = applicationStructureInJson.concat("\n");
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return applicationStructureInJson;
    }

    // This filter will be slightly different than the others in that we want to break up the sorting into multiple
    // lists of CtClass objects by role. This filter function will be used to cache the classes in a private map
    // which will then be used in process
    protected final boolean filter(CtClass clazz) {
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) clazz.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        if(annotationsAttribute != null) {
            Annotation[] classAnnotations = annotationsAttribute.getAnnotations();
            Annotation[] methodAnnotations = null;

            List<String> defaultPerms = new ArrayList<>();
            for (Annotation annotation : classAnnotations) {
                if (annotation.getTypeName().equals("javax.annotation.security.RolesAllowed")) {
                    Set<String> names = annotation.getMemberNames();
                    for (String name : names) {
                        MemberValue value = annotation.getMemberValue(name);
                        if (value instanceof ArrayMemberValue) {
                            ArrayMemberValue amv = (ArrayMemberValue)value;
                            MemberValue[] memberValues = amv.getValue();
                            for (MemberValue mv : memberValues) {
                                String val = mv.toString().replace("\"", "");
                                defaultPerms.add(val);
                            }
                        }
                    }
                }
            }

            for ( CtMethod method : clazz.getDeclaredMethods() ) {
                MethodInfo methodInfo = method.getMethodInfo();
                AnnotationsAttribute attr = (AnnotationsAttribute) methodInfo.getAttribute(AnnotationsAttribute.visibleTag);
                methodAnnotations = attr.getAnnotations();
                nodes.put(method.getLongName(), new ArrayList<>());
                roles.put(method.getLongName(), new ArrayList<>());
                for (Annotation annotation : methodAnnotations) {
                    if (annotation.getTypeName().equals("javax.annotation.security.RolesAllowed")) {
                        Set<String> names = annotation.getMemberNames();
                        for (String name : names) {
                            MemberValue value = annotation.getMemberValue(name);
                            if (value instanceof ArrayMemberValue) {
                                ArrayMemberValue amv = (ArrayMemberValue)value;
                                MemberValue[] memberValues = amv.getValue();
                                for (MemberValue mv : memberValues) {
                                    String val = mv.toString().replace("\"", "");
                                    roles.get(method.getLongName()).add(val);
                                }
                            }
                        }
                    } else if (annotation.getTypeName().equals("javax.annotation.security.PermitAll")) {
                        if (defaultPerms.size() > 0) {
                            roles.get(method.getLongName()).addAll(defaultPerms);
                        } else {
                            roles.get(method.getLongName()).add("SEER_DEFAULT_ALL_ROLES_PERMITTED");
                        }
                    }

                    if (annotation.getTypeName().equals("javax.annotation.security.RolesAllowed") ||
                        annotation.getTypeName().equals("javax.annotation.security.PermitAll")) {
                        try {
                            method.instrument(
                                    new ExprEditor() {
                                        public void edit(MethodCall m) {
                                            List<String> subMethodList = nodes.get(method.getLongName());

                                            CtMethod ctMethod;
                                            try {
                                                ctMethod = m.getMethod();
                                            } catch (Exception ex) {
                                                return;
                                            }
                                            subMethodList.add(ctMethod.getLongName());
                                        }
                                    }
                            );
                        } catch (CannotCompileException cex) {
                            System.out.println(cex.toString());
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
