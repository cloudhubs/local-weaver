package edu.baylor.ecs.cfgg.processor.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.baylor.ecs.cfgg.processor.repository.EvaluatorRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

@Service
public class ProcessorService {

    @Autowired
    EvaluatorRepository evaluatorRepository;


    public String generateSourceCode() throws IOException, URISyntaxException {

        //TODO : support module-module comms
        //String json = evaluatorRepository.getGraphInJsonFormat();

        // temp : read in from mock file

        URL jsonName = ProcessorService.class.getClassLoader().getResource("mocks/sample1.json");
        File jsonFile = new File(jsonName.toURI());
        Scanner input = new Scanner(jsonFile);

        String json = "";
        while(input.hasNext()) {
            json += input.nextLine();
        }

        // endtemp

        //ToDo: process json to source code

        String graph = processJson(json);

        // temp : just print json text

        return graph;

        // endtemp

    }

    private String processJson(String json) throws IOException {

        JSONObject object = new JSONObject(json);

        HashMap<String, ArrayList<ArrayList<String>>> map;
        map = new ObjectMapper().readValue(json, HashMap.class);

        List<String> disconnectedNodes = new ArrayList<>();

        String graph = "digraph {\n";

        for (String parent : map.keySet()) {
            String modParent = parent.substring(parent.indexOf('[') + 1, parent.lastIndexOf(']'))
                    .replace(", ", "::");
            ArrayList<ArrayList<String>> children = map.get(parent);
            if (children.size() == 0) {
                disconnectedNodes.add(modParent);
            } else {
                for (ArrayList<String> child : children) {
                    graph += "  \"" + modParent + "\" -> \""
                            + child.get(0) + "::" + child.get(1) + "\"\n";
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
