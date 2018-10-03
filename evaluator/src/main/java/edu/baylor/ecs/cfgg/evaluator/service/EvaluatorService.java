package edu.baylor.ecs.cfgg.evaluator.service;

import edu.baylor.ecs.cfgg.evaluator.repository.EvaluatorRepository;
import javassist.*;
import javassist.bytecode.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public abstract class EvaluatorService {

    @Autowired
    private EvaluatorRepository evaluatorRepository;

    ClassPathScanner classPathScanner = new ClassPathScanner();
    Set<ClassFile> classFileSet;

    public String deriveStructure(){

        ClassPool cp = ClassPool.getDefault();

        classPathScanner.scanUri("file://///Users/walkerand/Documents/Research/jars/sm-core-2.2.0-SNAPSHOT.jar");
        classFileSet = classPathScanner.getClasses();
        classPathScanner.scanUri("file://///Users/walkerand/Documents/Research/jars/sm-core-model-2.2.0-SNAPSHOT.jar");
        classFileSet.addAll(classPathScanner.getClasses());
        classPathScanner.scanUri("file://///Users/walkerand/Documents/Research/jars/sm-core-modules-2.2.0-SNAPSHOT.jar");
        classFileSet.addAll(classPathScanner.getClasses());

        List<CtClass> classes = new ArrayList<>();

        for(ClassFile classFile : classFileSet){

            CtClass clazz = null;
            try {
                clazz = cp.makeClass(classFile);
            } catch (Exception e){
                System.out.println(e.toString());
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
