package edu.baylor.ecs.seer.lweaver.service;

import javassist.*;
import javassist.bytecode.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class EvaluatorService {

    @Autowired
    protected ResourceLoader resourceLoader;

    protected ClassPathScanner classPathScanner = new ClassPathScanner();

    public String deriveStructure(){

        String directory = new File("").getAbsolutePath();

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
        } catch(Exception e){
            e.printStackTrace();
        }

        System.out.println(fileNames);

        List<Resource> resources = new ArrayList<>();
        for(String file : fileNames){
            Resource classPathResource = resourceLoader.getResource("file:" + file);
            resources.add(classPathResource);
        }

        Set<ClassFile> classFileSet = new HashSet<>();
        for(Resource resource : resources){
            try{
                classPathScanner.scanUri(resource.getURI().toString());
                classFileSet.addAll(classPathScanner.getClasses());
            } catch (Exception e){
                System.out.println("IOException: " + e.toString());
            }
        }

        ClassPool cp = ClassPool.getDefault();
        List<CtClass> classes = new ArrayList<>();

        for(ClassFile classFile : classFileSet){

            CtClass clazz = null;
            try {
                clazz = cp.makeClass(classFile);
            } catch (Exception e){
                System.out.println("Failed to make class:" + e.toString());
                break;
            }

            if(filter(clazz)){
                classes.add(clazz);
            }
        }

        return process(classes);
    }

    protected abstract String process(List<CtClass> classes);
    protected abstract boolean filter(CtClass clazz);

}
