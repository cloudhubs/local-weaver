package edu.baylor.ecs.seer.lweaver.controller;

import edu.baylor.ecs.seer.lweaver.service.*;
import edu.baylor.ecs.seer.lweaver.service.adapter.RemoteEvaluatorServiceAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/local-weaver/remote")
public class LocalWeaverRemoteController {

    private FlowStructureService flowStructureService;
    private BytecodeFlowStructureService bytecodeFlowStructureService;
    private DependencyService dependencyService;
    private DataModelService dataModelService;
    private SecurityService securityService;

    @Autowired
    public LocalWeaverRemoteController(FlowStructureService flowStructureService, BytecodeFlowStructureService bytecodeFlowStructureService, DependencyService dependencyService, DataModelService dataModelService, SecurityService securityService) {
        this.flowStructureService = flowStructureService;
        this.bytecodeFlowStructureService = bytecodeFlowStructureService;
        this.dependencyService = dependencyService;
        this.dataModelService = dataModelService;
        this.securityService = securityService;
    }

    // The flow structure endpoint generates a structure showing what methods are called by what classes and what
    // methods those methods call
    @RequestMapping(value = "/home")
    @GetMapping
    public String home(@PathVariable String arg) {
        return "Welcome!";
    }

    // The flow structure endpoint generates a structure showing what methods are called by what classes and what
    // methods those methods call
    @RequestMapping(value = "/flowStructure")
    @GetMapping
    public String generateFlowStructure(@PathVariable String path) {
        return (new RemoteEvaluatorServiceAdapter(flowStructureService)).deriveStructure();
    }

    // The bytecode flow structure endpoint generates a structure of bytecode instructions as a tree, filtering out
    // the unnecessary ones to be used to analyze if and for cycles
    @RequestMapping(value = "/bytecodeFlowStructure")
    @GetMapping
    public String generateBytecodeFlowStructure(@PathVariable String path) {
        return (new RemoteEvaluatorServiceAdapter(bytecodeFlowStructureService)).deriveStructure();

    }

    // The dependency endpoint generates a list of what outside packages are used and how many times they are used in
    // the application
    @RequestMapping(value = "/dependency")
    @GetMapping
    public String generateDependencyStructure(@PathVariable String path) {
        return (new RemoteEvaluatorServiceAdapter(dependencyService)).deriveStructure();
    }

    // The data model endpoint generates a structure of entity objects and their member variables along with the
    // annotation values on those member variables
    @RequestMapping(value = "/dataModel")
    @GetMapping
    public String generateDataModelStructure(@PathVariable String path) {
        return (new RemoteEvaluatorServiceAdapter(dataModelService)).deriveStructure();
    }

    // The security endpoint generates a list for each role of what methods may be called by the role specified
    @RequestMapping(value = "/security")
    @GetMapping
    public String generateSecurityStructure(@PathVariable String path) {
        return (new RemoteEvaluatorServiceAdapter(securityService)).deriveStructure();
    }

}