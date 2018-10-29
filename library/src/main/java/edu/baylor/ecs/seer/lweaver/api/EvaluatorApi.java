package edu.baylor.ecs.seer.lweaver.api;

import edu.baylor.ecs.seer.lweaver.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/local-weaver")
public class EvaluatorApi {

    @Autowired
    private ApplicationEvaluatorService applicationEvaluatorService;

    @Autowired
    private BytecodeEvaluatorService bytecodeEvaluatorService;

    @Autowired
    private DependencyEvaluatorService dependencyEvaluatorService;

    @Autowired
    private EntityEvaluatorService entityEvaluatorService;

    @Autowired
    private SecurityEvaluatorService securityEvaluatorService;

    @RequestMapping(value = "/application")
    @GetMapping
    public String generateApplicationJsonString() {
        return applicationEvaluatorService.deriveStructure();
    }

    @RequestMapping(value = "/bytecode")
    @GetMapping
    public String generateBytecodeJsonString() {
        return bytecodeEvaluatorService.deriveStructure();
    }

    @RequestMapping(value = "/dependency")
    @GetMapping
    public String generateDependencyJsonString() {
        return dependencyEvaluatorService.deriveStructure();
    }

    @RequestMapping(value = "/entity")
    @GetMapping
    public String generateEntityJsonString() {
        return entityEvaluatorService.deriveStructure();
    }

    @RequestMapping(value = "/security")
    @GetMapping
    public String generateSecurityJsonString() {
        return securityEvaluatorService.deriveStructure();
    }

}
