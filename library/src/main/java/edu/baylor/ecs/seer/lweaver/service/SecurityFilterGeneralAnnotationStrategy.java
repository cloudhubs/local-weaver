package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.security.HttpType;
import edu.baylor.ecs.seer.common.security.SecurityRootMethod;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class SecurityFilterGeneralAnnotationStrategy implements SecurityFilterStrategy {

    /*getSecurityMethods*/
    @Override
    public boolean doFilter(CtClass clazz,
                            Set<SecurityRootMethod> securityMethods) {

        if(clazz.getPackageName().startsWith("java.")){
            return true;
        }

        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) clazz.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        if(annotationsAttribute == null){
            return true;
        }
        Annotation[] clazzAnnotations = annotationsAttribute.getAnnotations();

        boolean isController = false;
        for(Annotation annotation : clazzAnnotations){
            if(annotation.getTypeName().equals("org.springframework.web.bind.annotation.RestController")){
                isController = true;
                break;
            }
        }

        if(isController) {
            CtMethod[] methods = clazz.getMethods();
            for (CtMethod ctMethod : methods) {

                if(ctMethod.getLongName().startsWith("java.")){
                    continue;
                }

                MethodInfo methodInfo = ctMethod.getMethodInfo();
                AnnotationsAttribute attr = (AnnotationsAttribute) methodInfo.getAttribute(AnnotationsAttribute.visibleTag);

                List<Annotation> allMethodAnnotations = new ArrayList<>();

                SecurityRootMethod rootMethod = new SecurityRootMethod(ctMethod.getLongName());
                if (securityMethods.stream().noneMatch(x -> x.getMethodName().equals(ctMethod.getLongName()))) {
                    securityMethods.add(rootMethod);
                } else {
                    rootMethod = securityMethods
                            .stream()
                            .filter(x -> x.getMethodName().equals(ctMethod.getLongName()))
                            .findFirst()
                            .get();
                }

                CtClass[] params = new CtClass[0];
                try {
                    params = ctMethod.getParameterTypes();
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }

                if(params.length > 0) {
                    List<CtClass> streamable = Arrays.asList(params);

                    List<String> params_s = streamable
                                                .stream()
                                                .map(CtClass::getName)
                                                .collect(Collectors.toList());

                    rootMethod.setParameters(params_s);
                }

                if (attr != null) {
                    Annotation[] methodAnnotations = attr.getAnnotations();
                    allMethodAnnotations.addAll(Arrays.asList(methodAnnotations));

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
                                        rootMethod.getRoles().add(val);
                                    }
                                }
                            }

                            try {
                                ctMethod.instrument(
                                    new ExprEditor() {
                                        public void edit(MethodCall m) {

                                            String fullSignature = "";
                                            try{
                                                CtMethod meth = m.getMethod();
                                                fullSignature = meth.getLongName();
                                            } catch (NotFoundException e){
                                                String className = m.getClassName();
                                                String methodName = m.getMethodName();
                                                String signature = sigToPackage(m.getSignature());
                                                fullSignature = className.concat(".").concat(methodName).concat(signature);
                                            }

                                            final String sig = fullSignature;

                                            if (!fullSignature.startsWith("java.")) {
                                                securityMethods
                                                        .stream()
                                                        .filter(x -> x.getMethodName()
                                                                .equals(ctMethod.getLongName()))
                                                        .findFirst()
                                                        .ifPresent(mthd -> mthd.getChildMethods().add(sig));
                                            }
                                        }
                                    }
                                );
                            } catch (CannotCompileException cex) {
                                System.out.println(cex.toString());
                                return false;
                            }
                        } else if (annotation.getTypeName().equals("org.springframework.web.bind.annotation.PostMapping")) {
                            rootMethod.setHttpType(HttpType.POST);
                        } else if (annotation.getTypeName().equals("org.springframework.web.bind.annotation.GetMapping")){
                            rootMethod.setHttpType(HttpType.GET);
                        } else if (annotation.getTypeName().equals("org.springframework.web.bind.annotation.PutMapping")){
                            rootMethod.setHttpType(HttpType.PUT);
                        } else if (annotation.getTypeName().equals("org.springframework.web.bind.annotation.DeleteMapping")){
                            rootMethod.setHttpType(HttpType.DELETE);
                        } else if (annotation.getTypeName().equals("org.springframework.web.bind.annotation.PatchMapping")){
                            rootMethod.setHttpType(HttpType.PATCH);
                        } else if (annotation.getTypeName().equals("org.springframework.web.bind.annotation.RequestMapping")){
                            rootMethod.setHttpType(HttpType.NONE);
                        }
                    }
                } else {
                    rootMethod.getRoles().add("SEER_ALL_ACCESS_ALLOWED");
                    rootMethod.setHttpType(HttpType.NONE);
                }
            }
        }

        return true;
    }

    private String sigToPackage(String sig){
        // Removes the return value
        String pckge = sig.substring(0, sig.lastIndexOf(")") + 1);

        // Remove parentheses for parsing - will be added at then end
        pckge = pckge.replaceAll("[(]", "");
        pckge = pckge.replaceAll("[)]", "");

        // Ljava/lang/Object; -> java/lang/Object;
        if(pckge.startsWith("L")){
            pckge = pckge.substring(1);
        }

        // java/lang/Object; -> java.lang.Object;
        pckge = pckge.replaceAll("/", ".");

        // java.lang.Object; -> java.lang.Object
        if(pckge.contains(";")){
            int count = StringUtils.countOccurrencesOf(pckge, ";");
            if(count == 1){
                pckge = pckge.replaceAll(";", "");
            } else {
                int lastNdx = pckge.lastIndexOf(";");
                pckge = pckge.substring(0, lastNdx);
                pckge = pckge.replaceAll(";", ", ");
            }
        }

        // java.lang.Object -> (java.lang.Object)
        pckge = "(" + pckge + ")";
        return pckge;
    }
}
