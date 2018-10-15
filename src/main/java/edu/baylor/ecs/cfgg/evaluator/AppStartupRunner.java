package edu.baylor.ecs.cfgg.evaluator;

import edu.baylor.ecs.cfgg.evaluator.service.ApplicationEvaluatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AppStartupRunner implements ApplicationRunner {

    @Autowired
    ApplicationEvaluatorService evaluatorService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println(evaluatorService.deriveStructure());
    }
}
