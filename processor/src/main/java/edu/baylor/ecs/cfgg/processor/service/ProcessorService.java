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


    public List<String> generateSourceCode() throws IOException, URISyntaxException {

        //TODO : support module-module comms
        String json = evaluatorRepository.getGraphInJsonFormat();

        List<String> graph = processJson(json);

        return graph;

    }

    private List<String> processJson(String json) throws IOException {

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
            StringBuilder dotGraph = new StringBuilder();
            dotGraph.append("strict digraph {\n");
            for (String line : graph) {
                dotGraph.append("  " + line + "\n");
            }
            dotGraph.append("}\n");
            dotGraphs.add(dotGraph.toString());
        }

        return dotGraphs;
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
                if (children.contains(checkVal)) {
                    processParent(map, graphs, checkVal, children, graph);
                }
            }
        }
    }

}
