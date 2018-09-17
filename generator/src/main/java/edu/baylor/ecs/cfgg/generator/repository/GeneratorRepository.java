package edu.baylor.ecs.cfgg.generator.repository;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class GeneratorRepository {

    public String getGraphSourceCode(){
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL("http://127.0.0.1:5003/processor/sourceCode");
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