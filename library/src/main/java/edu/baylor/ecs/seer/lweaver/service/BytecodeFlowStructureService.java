package edu.baylor.ecs.seer.lweaver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.baylor.ecs.seer.common.FlowNode;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.*;
import javassist.bytecode.analysis.FramePrinter;
import javassist.bytecode.annotation.Annotation;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    BytecodeFlowStructureService self;

    public final String process(List<CtClass> classes){
        // Setup some initial objects
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        FramePrinter fp = new FramePrinter(out);
        FramePrinter fpSout = new FramePrinter(System.out);
        String applicationStructureInJson = "";

        classes = self.getServiceClasses(classes);


        // Loop through every class in the array
        for(CtClass clazz : classes){

//            ClassFile cf = clazz.getClassFile();
//            MethodInfo minfo = cf.getMethod("move");    // we assume move is not overloaded.
//            CodeAttribute ca = minfo.getCodeAttribute();
//            CodeIterator ci = ca.iterator();
//            while (ci.hasNext()) {
//                int index = 0;
//                try {
//                    index = ci.next();
//                } catch (BadBytecode badBytecode) {
//                    badBytecode.printStackTrace();
//                }
//                int op = ci.byteAt(index);
//                System.out.println(Mnemonic.OPCODE[op]);
//            }

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
        List<Map<Integer, FlowNode>> trees = self.processBytecode(bytecode);

        try {
            applicationStructureInJson = new ObjectMapper().writeValueAsString(trees);
        } catch (Exception e){
            System.out.println(e.toString());
        }

        return applicationStructureInJson;
        //return sb.toString();
    }

//    public final boolean filter(CtClass clazz){
//        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) clazz.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
//        if(annotationsAttribute != null) {
//            Annotation[] annotations = annotationsAttribute.getAnnotations();
//            for (Annotation annotation : annotations) {
//                if (annotation.getTypeName().equals("org.springframework.stereotype.Service") || annotation.getTypeName().equals("org.springframework.stereotype.Component")){
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    private List<CtClass> getServiceClasses(List<CtClass> allClasses){
        List<CtClass> entityClasses = new ArrayList<>();
        for (CtClass ctClass: allClasses
        ) {
            AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
            if(annotationsAttribute != null) {
                Annotation[] annotations = annotationsAttribute.getAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation.getTypeName().equals("javax.ejb.Stateless")) {
                        entityClasses.add(ctClass);
                    }
                }
            }
        }
        return entityClasses;
    }

    // The purpose of preprocessing is to remove any methods that are abstract or have no body and also
    // to break up each method into a separate string

    //http://www.javassist.org/tutorial/tutorial3.html

    // TODO: test
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

        // Reset the current method
        currentMethod = "";

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

    // TODO: test
    // TODO: refactor
    // Processing the bytecode will create a tree of nodes that will show the flow of the nodes
    public List< Map<Integer, FlowNode> > processBytecode(String bytecode){

        List< Map<Integer, FlowNode> > roots = new ArrayList<>();

        // Filter out garbage lines in the bytecode
        List<String> processed = self.preprocessBytecode(bytecode);

        for(String s : processed) {

            // Initialize the map for post-processing
            Map<Integer, FlowNode> map = new HashMap<>();

            // Split the method bytecode string based on newlines so each command is a different index
            String[] arr = s.split("\n");

            FlowNode current = null;
            FlowNode root = null;

            // Loop through every command, skipping the method header
            for(int i = 1; i < arr.length; i++) {

                // Get the current command
                String line = arr[i];

                // Check that the command exists
                if (line != null && !line.equals("")) {
                    // Split the command into it's metadata
                    String[] split = line.trim().split(" ");

                    // Pull out the id and the body of the command
                    String id = split[0].substring(0, split[0].length() - 1);
                    String command = split[1];
                    String type = "general";

                    // Determine the type of the command
                    if (command.equals("goto")){
                        type = "goto";
                    } else if (command.startsWith("if")) {
                        type = "conditional";
                    } else if (command.startsWith("invoke")) {
                        type = "method";
                    } else if (command.contains("return")){
                        type = "return";
                    }

                    // Create a new flowNode with id and type
                    FlowNode flowNode = new FlowNode(id, type);
                    // Set the flowNode's raw data
                    flowNode.setRaw(line);

                    // Put the flowNode into the map for post-processing later
                    map.put(Integer.parseInt(id), flowNode);

                    // If the root to this tree is null, initialize a new root
                    if (current == null) {
                        current = flowNode;
                        // Set the superroot
                        root = current;
                    } else {
                        // If there is a currentNode then assume sequential ordering and add the new child
                        current.addChild(flowNode);
                        current = flowNode;
                    }
                }
            }

            // Post-Processing for building correct ordering
            for (Map.Entry<Integer, FlowNode> entry : map.entrySet()){

                // If the node is a conditional:
                //      add the new child from map
                if(entry.getValue().getType().equals("conditional")){
                    String[] values = entry.getValue().getRaw().split(" ");
                    Integer next = Integer.parseInt(values[2]);
                    FlowNode n = map.get(next);
                    entry.getValue().addChild(n);
                }
                // If the node is a goto
                //      break the existing condition
                //      add the new child from map
                else if (entry.getValue().getType().equals("goto")){
                    String[] values = entry.getValue().getRaw().split(" ");

                    Iterator<Integer> it = entry.getValue().getChildren().iterator();
                    while (it.hasNext()) {
                        it.next();
                        it.remove();
                    }

                    Integer next = Integer.parseInt(values[2]);
                    FlowNode n = map.get(next);
                    entry.getValue().addChild(n);
                }

            }

            // If the tree exists then add it to the structure
            if(root != null) {
                // Before adding it, post-process the map
                roots.add(self.postProcessBytecode(map));
            }
        }

        return roots;
    }

    // TODO: test
    // Post processing is optional but will remove any filler nodes so the only ones that remain are the initial
    // instruction and any logic nodes or method call nodes
    public Map<Integer, FlowNode> postProcessBytecode(Map<Integer, FlowNode> map){

        Set<Integer> importantNodes = new HashSet<>();
        importantNodes.add(0);

        // Filter out unimportant node
        for (Map.Entry<Integer, FlowNode> entry : map.entrySet()){

            String type = entry.getValue().getType();

            // If it's a method or return then we want it so add
            if(type.equals("method") || type.equals("return")){
                importantNodes.add(entry.getKey());
            }

            // If it's a conditional or goto then we want the node and both its children, even if one of the children
            // is a normal node
            if(type.equals("conditional") || type.equals("goto")){
                // Add the node
                importantNodes.add(entry.getKey());
                // Add the children
                importantNodes.addAll(entry.getValue().getChildren());
            }
        }

        // Sort the list of nodes by their key
        List<Integer> sortedList = new ArrayList<>(importantNodes);
        Collections.sort(sortedList);

        // Rebuild tree
        for(int i = 0; i < sortedList.size() - 1; i++){
            Integer key = sortedList.get(i);
            // If it is a conditional or a goto node then it's children are already correct
            if(!map.get(key).getType().equals("conditional") && !map.get(key).getType().equals("goto")){
                // Clear the existing children and add the next child
                map.get(key).setChildren(new ArrayList<>());
                map.get(sortedList.get(i+1)).setParents(new ArrayList<>());
                map.get(key).addChild(map.get(sortedList.get(i+1)));
            } else if (map.get(key).getType().equals("goto")){
                map.get(sortedList.get(i + 1)).removeParent(map.get(key));
            }
        }

        // Remove any nodes from the map that aren't needed anymore
        Iterator<Integer> it = map.keySet().iterator();
        map.keySet().removeIf(e -> !sortedList.contains(e));

        // Sort the map
        Map<Integer, FlowNode> sortedMap = new TreeMap<>(map);

        // Return the first node
        return sortedMap;
    }
}
