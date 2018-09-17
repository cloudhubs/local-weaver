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

        List<String> parents = new ArrayList<>();
        List<String> children = new ArrayList<>();

        for (String parent : map.keySet()) {
            Map<String, ArrayList<ArrayList<String>>> tempMap = map;
            tempMap.remove(parent);
            Boolean isChild = false;
            for (String key : map.keySet()) {
                for (List<String> list : map.get(key)) {
                    for (String value : list) {
                        if (value.equals(parent)) {
                            isChild = true;
                            break;
                        }
                    }
                    if (isChild) {
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

        // TODO: process parents and children to build graphs

        return "";
    }

}
