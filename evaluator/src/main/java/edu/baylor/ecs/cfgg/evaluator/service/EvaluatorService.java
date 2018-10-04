package edu.baylor.ecs.cfgg.evaluator.service;

import edu.baylor.ecs.cfgg.evaluator.repository.EvaluatorRepository;
import javassist.*;
import javassist.bytecode.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class EvaluatorService {

    @Autowired
    private EvaluatorRepository evaluatorRepository;

    ClassPathScanner classPathScanner = new ClassPathScanner();
    Set<ClassFile> classFileSet;

    public String deriveStructure(){

        String directory = "/Users/walkerand/Documents/Research/jars/";

        Path start = Paths.get(directory);
        int maxDepth = 15;
        List<String> fileNames = new ArrayList<>();
        try {
            Stream<Path> stream = Files.find(start, maxDepth, (path, attr) -> String.valueOf(path).toLowerCase().endsWith(".jar") || String.valueOf(path).toLowerCase().endsWith(".war"));
            fileNames = stream
                        .sorted()
                        .map(String::valueOf)
                        .filter((path) -> {
                            return String.valueOf(path).toLowerCase().endsWith(".jar") || String.valueOf(path).toLowerCase().endsWith(".war");
                        })
                        .collect(Collectors.toList());
        } catch(Exception e){
            e.printStackTrace();
        }

        classFileSet = new HashSet<>();
        for(String file : fileNames){
            classPathScanner.scanUri("file:////" + file);
            classFileSet.addAll(classPathScanner.getClasses());
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
