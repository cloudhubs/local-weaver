package edu.baylor.ecs.seer.lweaver.domain;

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

import java.util.*;

public class SecurityFilterGeneralAnnotationStrategy implements SecurityFilterStrategy {
    @Override
    public boolean doFilter(CtClass clazz,
                            Map<String, Set<String>> roles,
                            Map<String, Set<String>> nodes) {
        AnnotationsAttribute annotationsAttribute =
                (AnnotationsAttribute) clazz.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        if(annotationsAttribute != null) {
            Annotation[] classAnnotations = annotationsAttribute.getAnnotations();
            Annotation[] methodAnnotations;
            Annotation[] ifcMethodAnnotations;
            ArrayList<Annotation> allMethodAnnotations = new ArrayList<>();

            List<String> defaultPerms = getDefaultPerms(classAnnotations);

            try {
                for (CtMethod method : clazz.getDeclaredMethods()) {
                    CtMethod tempMethod = null;
                    MethodInfo methodInfo = method.getMethodInfo();
                    AnnotationsAttribute attr =
                            (AnnotationsAttribute) methodInfo.getAttribute(AnnotationsAttribute.visibleTag);
                    if (attr != null) {
                        methodAnnotations = attr.getAnnotations();
                        allMethodAnnotations.addAll(Arrays.asList(methodAnnotations));
                        CtClass[] ifcs = clazz.getInterfaces();
                        for ( CtClass ifc : ifcs ) {
                            for ( CtMethod meth : ifc.getDeclaredMethods() ) {
                                if (meth.getName().equals(method.getName())) {
                                    tempMethod = meth;
                                    AnnotationsAttribute attribute = (AnnotationsAttribute) meth.getMethodInfo()
                                            .getAttribute(AnnotationsAttribute.visibleTag);
                                    if (attribute != null) {
                                        ifcMethodAnnotations = attribute.getAnnotations();
                                        allMethodAnnotations.addAll(Arrays.asList(ifcMethodAnnotations));
                                    }
                                    break;
                                }
                            }
                        }
                        CtMethod modMethod = tempMethod == null ? method : tempMethod;

                        nodes.put(modMethod.getLongName(), new HashSet<>());
                        roles.put(modMethod.getLongName(), new HashSet<>());
                        for (Annotation annotation : allMethodAnnotations) {
                            if (annotation.getTypeName().equals("javax.annotation.security.RolesAllowed")) {
                                Set<String> names = annotation.getMemberNames();
                                for (String name : names) {
                                    MemberValue value = annotation.getMemberValue(name);
                                    if (value instanceof ArrayMemberValue) {
                                        ArrayMemberValue amv = (ArrayMemberValue) value;
                                        MemberValue[] memberValues = amv.getValue();
                                        for (MemberValue mv : memberValues) {
                                            String val = mv.toString().replace("\"", "");
                                            roles.get(modMethod.getLongName()).add(val);
                                        }
                                    }
                                }
                            } else if (annotation.getTypeName().equals("javax.annotation.security.PermitAll")) {
                                if (defaultPerms.size() > 0) {
                                    roles.get(modMethod.getLongName()).addAll(defaultPerms);
                                } else {
                                    roles.get(modMethod.getLongName()).add("SEER_DEFAULT_ALL_ROLES_PERMITTED");
                                }
                            }

                            if (annotation.getTypeName().equals("javax.annotation.security.RolesAllowed") ||
                                    annotation.getTypeName().equals("javax.annotation.security.PermitAll")) {
                                try {
                                    method.instrument(
                                            new ExprEditor() {
                                                public void edit(MethodCall m) {
                                                    Set<String> subMethodList = nodes.get(modMethod.getLongName());

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
            } catch (Exception ex ) {
                return false;
            }
        }
        return true;
    }

    private List<String> getDefaultPerms(Annotation[] classAnnotations) {
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
        return defaultPerms;
    }
}
