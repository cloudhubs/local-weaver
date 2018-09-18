package edu.baylor.ecs.cfgg.generator.service;

import edu.baylor.ecs.cfgg.generator.repository.GeneratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

@Service
public class GeneratorService {

    @Autowired
    private GeneratorRepository generatorRepository;

    @Autowired
    private RandomService randomService;

    public List<String> generateGraph() throws IOException {

        String sources = generatorRepository.getGraphSourceCode();
        List<String> sourceArray = Arrays.asList(sources.split("@"));

        for (String source: sourceArray
        ) {

            // Generate random string

            String randomAppendix = randomService.randomString();
            String generatedName = "graph" + randomAppendix + ".dot";

            // Write output

            PrintWriter writer = new PrintWriter("graphs/" + generatedName, "UTF-8");
            writer.println(source);
            writer.close();

            // Generate png

            String command = "dot -Tps graphs/" + generatedName + " -o graphs/" + randomAppendix + ".ps";
            Process proc = null;
            try {
                proc = Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Read the output from command

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String line = "";
            while((line = reader.readLine()) != null) {
                System.out.print(line + "\n");
            }

            try {
                proc.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        return Arrays.asList(sources.split(";"));
    }

}