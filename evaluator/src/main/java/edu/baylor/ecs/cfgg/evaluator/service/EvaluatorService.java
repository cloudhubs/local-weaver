package edu.baylor.ecs.cfgg.evaluator.service;

import edu.baylor.ecs.cfgg.evaluator.repository.LoaderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EvaluatorService {

    @Autowired
    private LoaderRepository loaderRepository;

    public String deriveApplicationStructure(){
        String classes = loaderRepository.getClasses();

        //ToDo: from classes derive application structure

        return "application structure";
    }

}
