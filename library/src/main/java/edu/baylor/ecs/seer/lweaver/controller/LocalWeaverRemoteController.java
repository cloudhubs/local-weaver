package edu.baylor.ecs.seer.lweaver.controller;

import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.lweaver.service.*;
import edu.baylor.ecs.seer.lweaver.service.adapter.RemoteEvaluatorServiceAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/local-weaver/remote")
public class LocalWeaverRemoteController {

    private SeerContextService seerContextService;
    private FlowStructureService flowStructureService;
    private BytecodeFlowStructureService bytecodeFlowStructureService;
    private DependencyService dependencyService;
    private DataModelService dataModelService;
    private SeerMsSecurityContextService securityService;

    @Autowired
    public LocalWeaverRemoteController(SeerContextService seerContextService, FlowStructureService flowStructureService, BytecodeFlowStructureService bytecodeFlowStructureService, DependencyService dependencyService, DataModelService dataModelService, SeerMsSecurityContextService securityService) {
        this.seerContextService = seerContextService;
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
    @PostMapping
    public SeerContext generateFlowStructure(@RequestBody SeerContext context) {
        context = seerContextService.getContextFromMicroservices(context);
        return (new RemoteEvaluatorServiceAdapter(flowStructureService)).deriveStructure(context);
    }

    // The bytecode flow structure endpoint generates a structure of bytecode instructions as a tree, filtering out
    // the unnecessary ones to be used to analyze if and for cycles
    @RequestMapping(value = "/bytecodeFlowStructure")
    @PostMapping
    public SeerContext generateBytecodeFlowStructure(@RequestBody SeerContext context) {
        return (new RemoteEvaluatorServiceAdapter(bytecodeFlowStructureService)).deriveStructure(context);
    }

    // The dependency endpoint generates a list of what outside packages are used and how many times they are used in
    // the application
    @RequestMapping(value = "/dependency")
    @PostMapping
    public SeerContext generateDependencyStructure(@RequestBody SeerContext context) {
        return (new RemoteEvaluatorServiceAdapter(dependencyService)).deriveStructure(context);
    }

    // The data model endpoint generates a structure of entity objects and their member variables along with the
    // annotation values on those member variables
    @RequestMapping(value = "/dataModel")
    @PostMapping
    public SeerContext generateDataModelStructure(@RequestBody SeerContext context) {
        return (new RemoteEvaluatorServiceAdapter(dataModelService)).deriveStructure(context);
    }

    // The security endpoint generates a list for each role of what methods may be called by the role specified
    @RequestMapping(value = "/security")
    @PostMapping
    public SeerContext generateSecurityStructure(@RequestBody SeerContext context) {
        return (new RemoteEvaluatorServiceAdapter(securityService)).deriveStructure(context);
    }

}