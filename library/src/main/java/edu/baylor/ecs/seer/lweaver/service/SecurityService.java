package edu.baylor.ecs.seer.lweaver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SecurityService extends EvaluatorService {

    private Map<String, Set<String>> roles = new HashMap<>();
    private Map<String, Set<String>> nodes = new HashMap<>();

    @Override
    public String deriveStructure() {

        String directory = new File("/Users/diehl/benchmark/tx/target/").getAbsolutePath();

        Path start = Paths.get(directory);
        int maxDepth = 15;
        List<String> fileNames = new ArrayList<>();
        try {
            Stream<Path> stream = Files.find(start, maxDepth, (path, attr) -> String.valueOf(path).toLowerCase().endsWith(".jar") || String.valueOf(path).toLowerCase().endsWith(".war"));
            fileNames = stream
                    .sorted()
                    .map(String::valueOf)
                    .filter((path) -> {
                        return (String.valueOf(path).toLowerCase().endsWith(".jar") ||
                                String.valueOf(path).toLowerCase().endsWith(".war")) &&
                                !String.valueOf(path).toLowerCase().contains("/.mvn/") &&
                                !String.valueOf(path).toLowerCase().startsWith("/usr/lib/jvm/") &&
                                !String.valueOf(path).toLowerCase().contains("/target/dependency/") &&
                                !String.valueOf(path).toLowerCase().contains("/gradle");
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(fileNames);

        List<Resource> resources = new ArrayList<>();
        for (String file : fileNames) {
            Resource classPathResource = resourceLoader.getResource("file:" + file);
            resources.add(classPathResource);
        }

        Set<ClassFile> classFileSet = new HashSet<>();
        for (Resource resource : resources) {
            try {
                classPathScanner.scanUri(resource.getURI().toString());
                classFileSet.addAll(classPathScanner.getClasses());
            } catch (Exception e) {
                System.out.println("IOException: " + e.toString());
            }
        }

        ClassPool cp = ClassPool.getDefault();
        List<CtClass> classes = new ArrayList<>();

        for (ClassFile classFile : classFileSet) {

            CtClass clazz = null;
            try {
                clazz = cp.makeClass(classFile);
                classes.add(clazz);
            } catch (Exception e) {
                System.out.println("Failed to make class:" + e.toString());
                break;
            }
        }

        List<CtClass> filtered = new ArrayList<>();
        for ( CtClass clazz : classes ) {
            if (filter(clazz)) {
                filtered.add(clazz);
            }
        }

        return process(filtered);
    }

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
            Annotation[] ifcMethodAnnotations = null;
            ArrayList<Annotation> allMethodAnnotations = new ArrayList<>();

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

            try {
                for (CtMethod method : clazz.getDeclaredMethods()) {
                    CtMethod tempMethod = null;
                    MethodInfo methodInfo = method.getMethodInfo();
                    AnnotationsAttribute attr = (AnnotationsAttribute) methodInfo.getAttribute(AnnotationsAttribute.visibleTag);
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
}
