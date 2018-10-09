package edu.baylor.ecs.cfgg.processor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import edu.baylor.ecs.cfgg.processor.repository.EvaluatorRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

@Service
public abstract class ProcessorService {

    @Autowired
    protected EvaluatorRepository evaluatorRepository;

    protected List<String> parents;
    protected List<String> children;
    protected String json;

    public abstract String generateSourceCode() throws IOException, URISyntaxException;

    protected final Map<String, List<List<String>>> populateParentsChildren(String json) throws IOException {
        JSONObject object = new JSONObject(json);

        Map<String, List<List<String>>> map;
        map = new ObjectMapper().readValue(json, HashMap.class);

        this.parents = new ArrayList<>();
        this.children = new ArrayList<>();

        for (String parent : map.keySet()) {
            Map<String, List<List<String>>> tempMap = new HashMap<>(map);
            tempMap.remove(parent);
            boolean isChild = false;
            for (String key : tempMap.keySet()) {
                for (List<String> list : tempMap.get(key)) {
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
                this.parents.add(parent);
            } else {
                this.children.add(parent);
            }
        }

        return map;
    }

    protected abstract String processJson(String json) throws IOException;

    protected void processParent(Map<String, List<List<String>>> map, List<List<String>> graphs,
                               String parent, List<String> graph) {
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
                if (this.children.contains(checkVal) && !parent.equals(checkVal)) {
                    processParent(map, graphs, checkVal, graph);
                }
            }
        }
    }

    protected List<String> splitJSONObjects() {
        List<String> splitJson = new ArrayList<>();
        Stack<Character> stack = new Stack<>();
        int start = 0;
        for (int i = this.json.indexOf('{'); i < this.json.length(); i++) {
            char ch = this.json.charAt(i);
            if (ch == '{') {
                if (stack.empty()) {
                    start = i;
                }
                stack.push(ch);
            } else if (ch == '}') {
                stack.pop();
                if (stack.empty()) {
                    splitJson.add(this.json.substring(start, i + 1));
                }
            }
        }
        return splitJson;
    }

}
