package edu.baylor.ecs.cfgg.generator.service;

import edu.baylor.ecs.cfgg.generator.repository.GeneratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

@Service
public class GeneratorService {

    @Autowired
    private GeneratorRepository generatorRepository;

    public String generateGraph() throws IOException {

        String source = generatorRepository.getGraphSourceCode();

        PrintWriter writer = new PrintWriter("graph.dot", "UTF-8");
        writer.println(source);
        writer.close();

<<<<<<< Updated upstream
        String command = "dot -Tps graph.dot -o outfile.ps";
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
=======
            PrintWriter writer = new PrintWriter("graphs/" + generatedName, "UTF-8");
            writer.println(source);
            writer.close();

            // Generate png

            String command = "dot -Tpng graphs/" + generatedName + " -o graphs/" + randomAppendix + ".png";
            Process proc = null;
            try {
                proc = Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Read the output from command
>>>>>>> Stashed changes

        // Read the output

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

        return source;
    }

}
