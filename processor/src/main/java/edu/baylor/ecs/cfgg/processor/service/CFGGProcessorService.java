package edu.baylor.ecs.cfgg.processor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CFGGProcessorService extends ProcessorService {

    @Override
    public String generateSourceCode() throws IOException, URISyntaxException {

        this.json = evaluatorRepository.getGraphInJsonFormat();

        Object jsonObj = new ObjectMapper().readValue(this.json, Object.class);
/*
        FileWriter file = new FileWriter("/Users/diehl/Documents/Research/entityJsonExample.json");
        String formatted = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(jsonObj);

        file.write(formatted);
        file.close();
*/
        List<String> splitJson = splitJSONObjects();

        String outString = "";
        for (String str : splitJson) {
            outString += processJson(str);
            if (splitJson.indexOf(str) != splitJson.size()) {
                outString += "@";
            }
        }

        return outString;

    }

    @Override
    protected String processJson(String json) throws IOException {

        Map<String, List<List<String>>> map = this.populateParentsChildren(json);

        List<List<String>> graphs = new ArrayList<>();
        for (String parent : this.parents) {
            List<String> graph = new ArrayList<>();
            processParent(map, graphs, parent, graph);
            graphs.add(graph);
        }

        List<String> dotGraphs = new ArrayList<>();
        for (List<String> graph : graphs) {
            if (graph.size() > 1) {
                StringBuilder dotGraph = new StringBuilder();
                dotGraph.append("strict digraph {\n  rankdir=LR;\n");
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

}
