package edu.baylor.ecs.cfgg.evaluator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.baylor.ecs.cfgg.evaluator.repository.EvaluatorRepository;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.analysis.FramePrinter;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class EvaluatorService {

    @Autowired
    private EvaluatorRepository evaluatorRepository;

    private Map<List<String>, List<List<String>>> formattedMap;
    private ClassPathScanner classPathScanner = new ClassPathScanner();
    private Set<ClassFile> classFileSet;

    public String deriveApplicationStructure(){

        ClassPool cp = ClassPool.getDefault();


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos, true, StandardCharsets.UTF_8);

        classPathScanner.scanUri("file://///Users/walkerand/Documents/Research/sm-core-2.2.0-SNAPSHOT.jar");
        classFileSet = classPathScanner.getClasses();
        classPathScanner.scanUri("file://///Users/walkerand/Documents/Research/sm-core-model-2.2.0-SNAPSHOT.jar");
        classFileSet.addAll(classPathScanner.getClasses());
        classPathScanner.scanUri("file://///Users/walkerand/Documents/Research/sm-core-modules-2.2.0-SNAPSHOT.jar");
        classFileSet.addAll(classPathScanner.getClasses());

        List<CtClass> classes = new ArrayList<>();
        for(ClassFile classFile : classFileSet){
            // Try to get the class as a CtClass
            CtClass clazz = null;
            try {
                clazz = cp.makeClass(classFile);
            } catch (Exception e){
                System.out.println(e.toString());
                break;
            }
            classes.add(clazz);
        }

        // Setup some initial objects
        formattedMap = new HashMap<>();
        String applicationStructureInJson = "";

        FramePrinter fp = new FramePrinter(out);

        // Loop through every class in the array
        for(CtClass clazz : classes){

            // Retrieve all the methods of a class
            CtMethod[] methods = clazz.getDeclaredMethods();

            // Loop through every method
            for(CtMethod method : methods){
                try {
                    fp.print(method);
                } catch (Exception e){
                    System.out.println(e.toString());
                }

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
                } catch (CannotCompileException e){
                    System.out.println(e.toString());
                }
            }
        }
        try {
            applicationStructureInJson = new ObjectMapper().writeValueAsString(formattedMap);
        } catch (Exception e){
            System.out.println(e.toString());
        }

        String bytecode = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        out.close();
        try {
            baos.close();
        } catch (Exception e){
            System.out.println(e.toString());
        }

        //System.out.println(bytecode);

        // Build the JSON and return it
        return applicationStructureInJson;
        //return bytecode;
    }
}
