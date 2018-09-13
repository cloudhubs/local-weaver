package edu.baylor.ecs.cfgg.processor.repository;

import edu.baylor.ecs.cfgg.processor.service.ProcessorService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

@Service
public class EvaluatorRepository {

    public String getGraphInJsonFormat() throws URISyntaxException, FileNotFoundException {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL("http://127.0.0.1:5002/evaluator/json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
        } catch (Exception e){
            e.printStackTrace();
        }

        return result.toString();
    }

}
