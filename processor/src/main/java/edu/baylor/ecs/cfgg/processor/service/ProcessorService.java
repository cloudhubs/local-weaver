package edu.baylor.ecs.cfgg.processor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.xpath.internal.operations.Bool;
import edu.baylor.ecs.cfgg.processor.repository.EvaluatorRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

@Service
public class ProcessorService {

    @Autowired
    EvaluatorRepository evaluatorRepository;


    public String generateSourceCode() throws IOException, URISyntaxException {

        //TODO : support module-module comms
        String json = evaluatorRepository.getGraphInJsonFormat();

        List<String> splitJson = splitJSONObjects(json);

        String outString = "";
        for (String str : splitJson) {
            outString += processJson(str);
            if (splitJson.indexOf(str) != splitJson.size()) {
                outString += "@";
            }
        }

        return outString;

    }

    private String processJson(String json) throws IOException {

        JSONObject object = new JSONObject(json);

        Map<String, List<List<String>>> map;
        map = new ObjectMapper().readValue(json, HashMap.class);

        List<String> parents = new ArrayList<>();
        List<String> children = new ArrayList<>();

        for (String parent : map.keySet()) {
            Map<String, List<List<String>>> tempMap = new HashMap<>(map);
            tempMap.remove(parent);
            boolean isChild = false;
            for (String key : map.keySet()) {
                for (List<String> list : map.get(key)) {
                    String compVal = "[" + list.get(0) + ", " + list.get(1) + "]";
                    if (parent.equals(compVal)) {
                        isChild = true;
                        break;
                    }
                }
                if (isChild) {
                    break;
                }
            }
            if (!isChild) {
                parents.add(parent);
            } else {
                children.add(parent);
            }
        }

        List<List<String>> graphs = new ArrayList<>();
        for (String parent : parents) {
            List<String> graph = new ArrayList<>();
            processParent(map, graphs, parent, children, graph);
            graphs.add(graph);
        }

        List<String> dotGraphs = new ArrayList<>();
        for (List<String> graph : graphs) {
            if (graph.size() > 1) {
                StringBuilder dotGraph = new StringBuilder();
                dotGraph.append("strict digraph {\n");
                for (String line : graph) {
                    dotGraph.append("  " + line + "\n");
                }
                dotGraph.append("}\n");
                dotGraphs.add(dotGraph.toString());
            }
        }

        StringBuilder output = new StringBuilder();
        List<String> tempGraphs = new ArrayList<>(dotGraphs);
        for (String graph : dotGraphs) {
            output.append(graph);
            tempGraphs.remove(graph);
            if (tempGraphs.size() > 0) {
                output.append("@");
            }
        }
        return output.toString();
    }

    private void processParent(Map<String, List<List<String>>> map, List<List<String>> graphs,
                               String parent, List<String> children, List<String> graph) {
        if (map.get(parent).size() == 0) {
            graph.add("\"" + parent.substring(parent.indexOf('['), parent.lastIndexOf(']')).replace(", ", "::")
                    .substring(parent.lastIndexOf('.') + 1) + "\";");
        } else {
            for (List<String> list : map.get(parent)) {
                graph.add("\"" + parent.substring(parent.indexOf('['), parent.lastIndexOf(']')).replace(", ", "::")
                        .substring(parent.lastIndexOf('.') + 1) + "\" -> \""
                        + list.get(0).substring(list.get(0).lastIndexOf('.') + 1) + "::"
                        + list.get(1).substring(list.get(1).lastIndexOf('.') + 1) + "\";");
                String checkVal = "[" + list.get(0) + ", " + list.get(1) + "]";
                if (children.contains(checkVal) && !parent.equals(checkVal)) {
                    processParent(map, graphs, checkVal, children, graph);
                }
            }
        }
    }

    private List<String> splitJSONObjects(String json) {
        List<String> splitJson = new ArrayList<>();
        Stack<Character> stack = new Stack<>();
        int start = 0;
        for (int i = json.indexOf('{'); i < json.length(); i++) {
            char ch = json.charAt(i);
            if (ch == '{') {
                if (stack.empty()) {
                    start = i;
                }
                stack.push(ch);
            } else if (ch == '}') {
                stack.pop();
                if (stack.empty()) {
                    splitJson.add(json.substring(start, i + 1));
                }
            }
        }
        return splitJson;
    }

}
