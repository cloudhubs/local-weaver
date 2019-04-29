package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.entity.EntityModel;
import edu.baylor.ecs.seer.common.flow.decision.*;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.*;
import javassist.bytecode.analysis.FramePrinter;
import javassist.bytecode.annotation.Annotation;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 *
 */
@Service
public class BytecodeFlowStructureService {

    public final List<Map<Integer, DecisionFlowNode>> process(List<CtClass> classes){
        // Setup some initial objects
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        FramePrinter fp = new FramePrinter(out);

        classes = getServiceClasses(classes);


        // Loop through every class in the array
        for(CtClass clazz : classes){
            // Retrieve all the methods of a class
            CtMethod[] methods = clazz.getDeclaredMethods();

            // Loop through every method
            for(CtMethod method : methods){

                try {
                    if(!method.getName().startsWith("get") && !method.getName().startsWith("set")) {
                        fp.print(method);
                        //fpSout.print(method);
                    }
                } catch (Exception e){
                    System.out.println(e.toString());
                }


            }
        }

        // Retrieve the bytecode from the ByteArrayOutputStream
        String bytecode = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        // Build the structure for parsing
        return processBytecode(bytecode);
    }

    private List<CtClass> getServiceClasses(List<CtClass> allClasses){
        List<CtClass> entityClasses = new ArrayList<>();
        for (CtClass ctClass: allClasses
        ) {
            AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
            if(annotationsAttribute != null) {
                Annotation[] annotations = annotationsAttribute.getAnnotations();
                for (Annotation annotation : annotations) {
                    if (isServiceClass(annotation)) {
                        entityClasses.add(ctClass);
                    }
                }
            }
        }
        return entityClasses;
    }

    // TODO: needs improvement
    private boolean isServiceClass(Annotation annotation) {
        return annotation.getTypeName().contains("javax.ejb")
                || annotation.getTypeName().contains("springframework.stereotype");
    }

    // The purpose of preprocessing is to remove any methods that are abstract or have no body and also
    // to break up each method into a separate string

    //http://www.javassist.org/tutorial/tutorial3.html

    // TODO: maybe refactor
    public List<String> preprocessBytecode(String bytecode){
        // Setup some initial structures
        List<String> storage = new ArrayList<>();
        String currentMethod = "";

        // Setup a reader around the bytecode string
        BufferedReader reader = new BufferedReader(new StringReader(bytecode));
        String line = null;
        do {

            // Read the line
            try{
                line = reader.readLine();
            } catch (Exception e){
                System.out.println(e.toString());
            }

            // If it is a real line
            if(line != null && !line.equals("")) {

                // Split it on spaces to extract out the metadata
                String[] arr = line.trim().split(" ");

                if(arr.length > 0) {
                    // Not one of the stack or locals lines
                    if (!arr[0].trim().equals("stack") && !arr[0].trim().equals("locals")) {

                        // If it doesn't start with a digit then it's a method heading
                        if(!Character.isDigit(arr[0].charAt(0))){

                            // If the current method has body, i.e this method is a new one, add it to the structure
                            if(!currentMethod.equals("")) {
                                storage.add(currentMethod);
                            }

                            // Reset the current method
                            currentMethod = "";
                        }

                        // Add the line to current method
                        currentMethod = currentMethod.concat(line + "\n");

                    }
                }
            }

        } while (line != null);

        // If the current method has body add it to the structure
        // This is in case there is a method in the pipeline
        if(!currentMethod.equals("")) {
            storage.add(currentMethod);
        }

        // This will remove any functions that have no body
        Iterator<String> it = storage.iterator();
        while (it.hasNext()) {
            String s = it.next();
            String[] arr = s.split("\n");
            if(!(arr.length > 1)){
                it.remove();
            }
        }

        return storage;
    }

    // Processing the bytecode will create a tree of nodes that will show the flow of the nodes
    public List< Map<Integer, DecisionFlowNode> > processBytecode(String bytecode){
        List< Map<Integer, DecisionFlowNode> > roots = new ArrayList<>();

        // Filter out garbage lines in the bytecode
        List<String> processed = preprocessBytecode(bytecode);

        for(String s : processed) {
            // Split the method bytecode string based on newlines so each command is a different index
            String[] arr = s.split("\n");

            DecisionFlowNode cur = null;
            DecisionFlowNode prev = null;

            // This will be the "root" of the graph
            DecisionFlowMethod flowMethod = new DecisionFlowMethod();
            cur = flowMethod;

            // Loop through every command, skipping the method header
            for(int i = 1; i < arr.length; i++) {
                // Get the current command
                String line = arr[i];

                // Check that the command exists
                if (line != null && !line.equals("")) {
                    // Split the command into it's metadata
                    String[] split = line.trim().split(" ");

                    DecisionFlowNode flowNode;

                    // Pull out the id and the body of the command
                    int id = Integer.parseInt(split[0].substring(0, split[0].length() - 1));
                    String command = split[1];

                    // Determine the type of the command
                    if (command.startsWith("if")) {
                        flowNode = parseIfStatement(id, arr);
                    } else if (command.startsWith("invoke")) {
                        flowNode = parseMethodStatement(id, arr);
                    } else {
                        flowNode = parseGeneralStatement(id, arr);
                    }
                    prev = cur;
                    cur = flowNode;

                    // Update links
                    cur.getParents().add(prev);
                    prev.getChildren().add(cur);
                }
            }

            flowMethod = postProcessBytecode(flowMethod);
        }

        return roots;
    }

    // Post processing is optional but will remove any filler nodes so the only ones that remain are the initial
    // instruction and any logic nodes or method call nodes
    public DecisionFlowMethod postProcessBytecode(DecisionFlowMethod method) {
        // Build a map allowing easy access to each node in the list by id
        Map<Integer, DecisionFlowNode> idToNode = new HashMap<>();

        // Assume that each node will have one or zero children
        DecisionFlowNode node = method.getChildren().stream().findFirst().orElse(null);
        while (node != null) {
            idToNode.put(node.getId(), node);
            node = node.getChildren().stream().findFirst().orElse(null);
        }



        return null;
    }

    private DecisionFlowConditional parseIfStatement(int id, String[] bytecodeLines) {
        String bytecode = bytecodeLines[id];
        DecisionFlowConditional flowConditional = new DecisionFlowConditional();
        flowConditional.setId(id);
        if (bytecode.contains("if_acmple")) {
            flowConditional.setDecisionOperator(DecisionOperator.LESS_OR_EQUAL);
            String op1;
            String op2;
            String op1Line = bytecodeLines[id - 1];
            String op2Line = bytecodeLines[id - 2];
            if (op1Line.contains("_")) {
                op1 = "local variable " + op1Line.split("_")[1];
            } else {
                op1 = op1Line.split(" ")[1];
            }
            if (op2Line.contains("_")) {
                op2 = "local variable " + op2Line.split("_")[1];
            } else {
                op2 = op1Line.split(" ")[1];
            }
            flowConditional.setLeft(op1);
            flowConditional.setRight(op2);
        } else {
            return null;
        }
        return flowConditional;
    }

    private DecisionFlowMethod parseMethodStatement(int id, String[] bytecodeLines) {
        String bytecode = bytecodeLines[id];
        DecisionFlowMethod flowMethod = new DecisionFlowMethod();
        flowMethod.setId(id);

        // TODO: parse method info

        return flowMethod;
    }

    private DecisionFlowGeneral parseGeneralStatement(int id, String[] bytecodeLines) {
        String bytecode = bytecodeLines[id];
        DecisionFlowGeneral flowGeneral = new DecisionFlowGeneral();
        flowGeneral.setId(id);
        flowGeneral.setDescription(bytecode.split(":")[1].trim());

        return flowGeneral;
    }
}
