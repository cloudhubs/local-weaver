package edu.baylor.ecs.seer.lweaver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
//import edu.baylor.ecs.seer.cfgg.flow.Node;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.analysis.FramePrinter;
import javassist.bytecode.annotation.Annotation;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class BytecodeFlowStructureService extends EvaluatorService {

    private StringBuilder sb;

    protected final String process(List<CtClass> classes){
        /*// Setup some initial objects
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos, true, StandardCharsets.UTF_8);
        FramePrinter fp = new FramePrinter(out);
        String applicationStructureInJson = "";
        sb = new StringBuilder();

        // Loop through every class in the array
        for(CtClass clazz : classes){

            // Retrieve all the methods of a class
            CtMethod[] methods = clazz.getDeclaredMethods();

            // Loop through every method
            for(CtMethod method : methods){
                try {
                    if(!method.getName().startsWith("get") && !method.getName().startsWith("set")) {
                        fp.print(method);
                    }
                } catch (Exception e){
                    System.out.println(e.toString());
                }


            }
        }

        // Retrieve the bytecode from the ByteArrayOutputStream
        String bytecode = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        // Build the structure for parsing
        List<Node> trees = processBytecode(bytecode);

        try {
            applicationStructureInJson = new ObjectMapper().writeValueAsString(trees);
        } catch (Exception e){
            System.out.println(e.toString());
        }

        //return applicationStructureInJson;
        return sb.toString();*/
        return "TODO";
    }

    protected final boolean filter(CtClass clazz){
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) clazz.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        if(annotationsAttribute != null) {
            Annotation[] annotations = annotationsAttribute.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.getTypeName().equals("org.springframework.stereotype.Service") || annotation.getTypeName().equals("org.springframework.stereotype.Component")){
                    return true;
                }
            }
        }
        return false;
    }

    // The purpose of preprocessing is to remove any methods that are abstract or have no body and also
    // to break up each method into a separate string
    /*private List<String> preprocessBytecode(String bytecode){
        // Setup some initial strctures
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
    private List<Node> processBytecode(String bytecode){

        List<Node> roots = new ArrayList<>();

        // Filter out garbage lines in the bytecode
        List<String> processed = preprocessBytecode(bytecode);

        for(String s : processed) {

            // Initialize the map for post-processing
            Map<Integer, Node> map = new HashMap<>();

            // Split the method bytecode string based on newlines so each command is a different index
            String[] arr = s.split("\n");

            Node current = null;
            Node root = null;

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

                    // Create a new node with id and type
                    Node node = new Node(id, type);
                    // Set the node's raw data
                    node.setRaw(line);

                    // Put the node into the map for post-processing later
                    map.put(Integer.parseInt(id), node);

                    // If the root to this tree is null, initialize a new root
                    if (current == null) {
                        current = node;
                        // Ret the superoot
                        root = current;
                    } else {
                        // If there is a currentNode then assume sequential ordering and add the new child
                        current.addChild(node, true);
                        current = node;
                    }
                }
            }

            // Post-Processing for building correct ordering
            for (Map.Entry<Integer, Node> entry : map.entrySet()){

                // If the node is a conditional:
                //      add the new child from map
                if(entry.getValue().getType().equals("conditional")){
                    String[] values = entry.getValue().getRaw().split(" ");
                    Integer next = Integer.parseInt(values[2]);
                    Node n = map.get(next);
                    entry.getValue().addChild(n, true);
                }
                // If the node is a goto
                //      break the existing condition
                //      add the new child from map
                else if (entry.getValue().getType().equals("goto")){
                    String[] values = entry.getValue().getRaw().split(" ");

                    Iterator<Node> it = entry.getValue().getChildren().iterator();
                    while (it.hasNext()) {
                        it.next();
                        it.remove();
                    }

                    Integer next = Integer.parseInt(values[2]);
                    Node n = map.get(next);
                    entry.getValue().addChild(n, true);
                }

            }

            // If the tree exists then add it to the structure
            if(root != null) {
                // Before adding it, post-process the map
                roots.add(postProcessBytecode(map));
            }
        }

        return roots;
    }

    // Post processing is optional but will remove any filler nodes so the only ones that remain are the initial
    // instruction and any logic nodes or method call nodes
    private Node postProcessBytecode(Map<Integer, Node> map){

        Set<Integer> importantNodes = new HashSet<>();
        importantNodes.add(0);

        // Filter out unimportant node
        for (Map.Entry<Integer, Node> entry : map.entrySet()){

            String type = entry.getValue().getType();

            // If it's a method or return then we want it so add
            if(type.equals("method") || type.equals("return")){
                importantNodes.add(entry.getKey());
            }

            // If it's a conditional or goto then we want the node and both it's children, even if one of the children
            // is a normal node
            if(type.equals("conditional") || type.equals("goto")){
                // Add the node
                importantNodes.add(entry.getKey());
                // Add the children
                for(int i = 0; i < entry.getValue().getChildren().size(); i++){
                    importantNodes.add(Integer.parseInt(entry.getValue().getChildren().get(i).getId()));
                }
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
                map.get(key).addChild(map.get(sortedList.get(i+1)), true);
            }
        }

        // Remove any nodes from the map that aren't needed anymore
        Iterator<Integer> it = map.keySet().iterator();
        while (it.hasNext()) {
            Integer key = it.next();
            if(!sortedList.contains(key)){
                it.remove();
            }
        }

        // Sort the map
        Map<Integer, Node> sortedMap = new TreeMap<>(map);

        // Print the sorted map
        printBytecodeTree(sortedMap);

        // Return the first node
        return map.get(0);
    }

    // This will print the tree for debugging purposes
    private void printBytecodeTree(Map<Integer, Node> map){

        // Loop through every node in the map
        for (Map.Entry<Integer, Node> entry : map.entrySet()){

            // Print the node and it's raw
            sb.append(entry.getValue().getRaw().concat("\n"));

            // Print the node's children
            for(int i = 0; i < entry.getValue().getChildren().size(); i++){
                sb.append("\t".concat(entry.getValue().getChildren().get(i).getRaw()).concat("\n"));
            }
        }

    }*/
}
