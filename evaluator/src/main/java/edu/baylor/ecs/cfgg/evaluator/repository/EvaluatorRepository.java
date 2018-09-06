package edu.baylor.ecs.cfgg.evaluator.repository;

import org.springframework.stereotype.Service;

@Service
public class EvaluatorRepository {

    public String getClasses(){
        return "edu.baylor.ecs.cfgg.evaluator.mock.MockClassA:edu.baylor.ecs.cfgg.evaluator.mock.MockClassB";
    }
}
