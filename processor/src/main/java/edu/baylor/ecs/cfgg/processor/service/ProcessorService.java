package edu.baylor.ecs.cfgg.processor.service;

import edu.baylor.ecs.cfgg.processor.repository.EvaluatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessorService {

    @Autowired
    EvaluatorRepository evaluatorRepository;


    public String generateSourceCode(){

        String json = evaluatorRepository.getGraphInJsonFormat();

        //ToDo: process json to source code

        return "source code";

    }

}
