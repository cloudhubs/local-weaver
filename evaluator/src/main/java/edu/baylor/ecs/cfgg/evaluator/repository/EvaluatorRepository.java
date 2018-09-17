package edu.baylor.ecs.cfgg.evaluator.repository;

import edu.baylor.ecs.cfgg.evaluator.mock.MockClassA;
import edu.baylor.ecs.cfgg.evaluator.mock.MockClassB;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EvaluatorRepository {

    public List<Class> getClasses(){
        List<Class> classes = new ArrayList<>();
        classes.add(MockClassA.class);
        classes.add(MockClassB.class);
        return classes;
    }
}
