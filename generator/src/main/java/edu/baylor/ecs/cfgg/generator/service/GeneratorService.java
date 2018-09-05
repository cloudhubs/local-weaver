package edu.baylor.ecs.cfgg.generator.service;

import edu.baylor.ecs.cfgg.generator.repository.ProcessorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class GeneratorService {

    @Autowired
    private ProcessorRepository processorRepository;

    public String generateGraph() throws IOException {

        String source = processorRepository.getGraphSourceCode();

        //ToDo: processing graph from source code

        //create file

        //write the source string into the file

        //do command to generate new file

        //take this file and persist that to database

        //or directly show the user

        String command = "ping -c 3 www.google.com";

        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        return "new graph";
    }

}
