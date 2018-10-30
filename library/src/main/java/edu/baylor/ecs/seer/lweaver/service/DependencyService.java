package edu.baylor.ecs.seer.lweaver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.*;
import javassist.bytecode.analysis.FramePrinter;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

@Service
public class DependencyService extends EvaluatorService {

    protected final String process(List<CtClass> classes){

        Map<String, Integer> map = new HashMap<>();
        String dependencyStructureInJson = "";

        // These objects are solely to trigger an exception if the class cannot be loaded
        OutputStream os = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(os);
        FramePrinter fp = new FramePrinter(out);

        // Loop through every class in the array
        for(CtClass clazz : classes){

            // Retrieve all the methods of a class
            CtMethod[] methods = clazz.getDeclaredMethods();

            // Loop through every method
            for(CtMethod method : methods){

                // Instrument the method to pull out the method calls
                try {
                    method.instrument(
                        new ExprEditor() {
                            public void edit(MethodCall m) {
                                // Unfortunately the only way to tell an outside dependency is to try and print it's
                                // internals and catch the exception
                                try {
                                    fp.print(m.getMethod());
                                } catch (Exception e){
                                    // Retrieve the name of the method from the exception
                                    String name = e.toString().substring(e.toString().lastIndexOf(":") + 2);
                                    // Filter out some exceptions that aren't relevant
                                    if(!e.toString().contains("is not found in")) {
                                        // Take off the name of the method
                                        String packageName = name.substring(0, name.lastIndexOf("."));

                                        // Put it in the map or update count if already in map
                                        int count = map.getOrDefault(packageName, 0);
                                        map.put(packageName, count + 1);
                                    }
                                }
                            }
                        }
                    );
                } catch (CannotCompileException e){
                    System.out.println("Instrument error: " + e.toString());
                }

            }
        }

        // Sort the map
        Map<String, Integer> sorted = map
                                        .entrySet()
                                        .stream()
                                        .sorted(comparingByValue())
                                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                                        LinkedHashMap::new));

        // Write the map into JSON
        try {
            dependencyStructureInJson = new ObjectMapper().writeValueAsString(sorted);
        } catch (Exception e){
            System.out.println(e.toString());
        }

        // Return the structure as JSON
        return dependencyStructureInJson;
    }

    // Need to add every class so that every class is accounted for
    protected final boolean filter(CtClass clazz){
        return true;
    }
}
