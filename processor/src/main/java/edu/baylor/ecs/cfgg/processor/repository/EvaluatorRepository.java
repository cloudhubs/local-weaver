package edu.baylor.ecs.cfgg.processor.repository;

import edu.baylor.ecs.cfgg.processor.service.ProcessorService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

@Service
public class EvaluatorRepository {

    public String getGraphInJsonFormat() throws URISyntaxException, FileNotFoundException {

        URL jsonName = ProcessorService.class.getClassLoader().getResource("mocks/sample1.json");
        File jsonFile = new File(jsonName.toURI());
        Scanner input = new Scanner(jsonFile);

        String json = "";
        while(input.hasNext()) {
            json += input.nextLine();
        }

        return json;
    }

}
