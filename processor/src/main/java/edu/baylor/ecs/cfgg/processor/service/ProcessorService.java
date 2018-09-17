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

        String graph = processJson(json);

        return graph;

    }

    private String processJson(String json) throws IOException {

        JSONObject object = new JSONObject(json);

        HashMap<String, ArrayList<ArrayList<String>>> map;
        map = new ObjectMapper().readValue(json, HashMap.class);

        List<String> disconnectedNodes = new ArrayList<>();

        Map<String, String> graphs = new HashMap<>();
        String graph = "digraph {\n";

        for (String parent : map.keySet()) {
            Boolean isChild = false;
            for (String key : map.keySet()) {
                for (ArrayList<String> list : map.get(key)) {
                    if (list.contains(parent)) {
                        isChild = true;
                        break;
                    }
                }
                if (isChild) {
                    break;
                }
            }
            if (!isChild) {
                graphs.put(parent, "digraph {\n");
            } else {

            }
        }

        for (String parent : map.keySet()) {
            String modParent = parent.substring(parent.indexOf('[') + 1, parent.lastIndexOf(']'))
                    .replace(", ", "::");
            modParent = modParent.substring(modParent.lastIndexOf('.') + 1);
            ArrayList<ArrayList<String>> children = map.get(parent);
            if (children.size() == 0) {
                disconnectedNodes.add(modParent);
            } else {
                for (ArrayList<String> child : children) {
                    graph += "  \"" + modParent + "\" -> \""
                            + child.get(0).substring(child.get(0).lastIndexOf('.') + 1) + "::" + child.get(1) + "\"\n";
                }
            }
        }

        for (String node : disconnectedNodes) {
            graph += "  \"" + node + "\";\n";
        }

        graph += "}\n";

        return graph;
    }

}
