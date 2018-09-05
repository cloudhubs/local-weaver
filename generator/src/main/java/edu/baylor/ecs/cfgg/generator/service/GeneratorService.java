package edu.baylor.ecs.cfgg.generator.service;

import edu.baylor.ecs.cfgg.generator.repository.ProcessorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeneratorService {

    @Autowired
    private ProcessorRepository processorRepository;

    public String generateGraph() {

        String source = processorRepository.getGraphSourceCode();

        //ToDo: processing graph from source code

        return "new graph";
    }

}
