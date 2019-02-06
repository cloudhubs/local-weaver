package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.common.context.SeerMsContext;
import edu.baylor.ecs.seer.common.context.SeerSecurityContext;
import edu.baylor.ecs.seer.common.security.SecurityMethod;
import edu.baylor.ecs.seer.lweaver.domain.SecurityFilterContext;
import edu.baylor.ecs.seer.lweaver.domain.SecurityFilterGeneralAnnotationStrategy;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.ClassFile;
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

    private List<SecurityMethod> methods = new ArrayList<>();

    @Override

    public SeerContext deriveStructure(SeerContext context) {


        String directory = new File(context
                                    .getRequest()
                                    .getPathToCompiledMicroservices()
                                    ).getAbsolutePath();

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

        return process(filtered, context);
    }

    SeerSecurityContext getMsSeerSecurityContext(List<CtClass> classes) {
        SeerSecurityContext seerSecurityContext = new SeerSecurityContext();

        for (CtClass clazz : classes) {
            if (filter(clazz)) {

            }
        }
    }

    protected final SeerContext process(List<CtClass> classes, SeerContext context){
        // TODO: add support to modify context and add the methods set
        // Note: this is a temporary solution, as it adds all data to all MS
        for (SeerMsContext seerMsContext : context.getMsContexts()) {
            // TODO: get the security context and modify it
        }

        return context;

        /* deprecated
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
        */
    }

    // This filter will be slightly different than the others in that we want to break up the sorting into multiple
    // lists of CtClass objects by role. This filter function will be used to cache the classes in a private map
    // which will then be used in process
    protected final boolean filter(CtClass clazz) {

        SecurityFilterContext securityFilterContext =
                new SecurityFilterContext(new SecurityFilterGeneralAnnotationStrategy());

        return securityFilterContext.doFilter(clazz, methods);

    }
}
