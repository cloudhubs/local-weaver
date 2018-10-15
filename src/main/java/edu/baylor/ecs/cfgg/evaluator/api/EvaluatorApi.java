package edu.baylor.ecs.cfgg.evaluator.api;

import edu.baylor.ecs.cfgg.evaluator.service.ApplicationEvaluatorService;
import edu.baylor.ecs.cfgg.evaluator.service.BytecodeEvaluatorService;
import edu.baylor.ecs.cfgg.evaluator.service.DependencyEvaluatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/evaluator")
public class EvaluatorApi {

    @Autowired
    private ApplicationEvaluatorService evaluatorService;

    @RequestMapping(value = "/json")
    @GetMapping
    public String generateJsonString() {

        String applicationStructureInJson = evaluatorService.deriveStructure();

        return applicationStructureInJson;

    }

}
