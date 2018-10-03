package edu.baylor.ecs.cfgg.evaluator.service;

import javassist.CtClass;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SecurityEvaluatorService extends EvaluatorService {

    private Map<String, List<CtClass>> roles;

    protected final String process(List<CtClass> classes){
        return "WIP";
    }

    protected final boolean filter(CtClass clazz){
        return true;
    }
}
