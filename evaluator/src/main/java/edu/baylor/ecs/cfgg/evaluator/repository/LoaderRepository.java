package edu.baylor.ecs.cfgg.evaluator.repository;

import org.springframework.stereotype.Service;

@Service
public class LoaderRepository {

    public String getClasses(){
        return "classes";
    }
}
