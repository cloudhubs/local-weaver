package edu.baylor.ecs.seer.lweaver.api;

import edu.baylor.ecs.seer.lweaver.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/local-weaver")
public class LocalWeaverApi {

    private FlowStructureService flowStructureService;
    private BytecodeFlowStructureService bytecodeFlowStructureService;
    private DependencyService dependencyService;
    private DataModelService dataModelService;
    private SecurityService securityService;

    @Autowired
    public LocalWeaverApi(FlowStructureService flowStructureService, BytecodeFlowStructureService bytecodeFlowStructureService, DependencyService dependencyService, DataModelService dataModelService, SecurityService securityService) {
        this.flowStructureService = flowStructureService;
        this.bytecodeFlowStructureService = bytecodeFlowStructureService;
        this.dependencyService = dependencyService;
        this.dataModelService = dataModelService;
        this.securityService = securityService;
    }

    // The flow structure endpoint generates a structure showing what methods are called by what classes and what
    // methods those methods call
    @RequestMapping(value = "/flowStructure")
    @GetMapping
    public String generateFlowStructure() {
        return flowStructureService.deriveStructure();
    }

    // The bytecode flow structure endpoint generates a structure of bytecode instructions as a tree, filtering out
    // the unnecessary ones to be used to analyze if and for cycles
    @RequestMapping(value = "/bytecodeFlowStructure")
    @GetMapping
    public String generateBytecodeFlowStructure() {
        return bytecodeFlowStructureService.deriveStructure();
    }

    // The dependency endpoint generates a list of what outside packages are used and how many times they are used in
    // the application
    @RequestMapping(value = "/dependency")
    @GetMapping
    public String generateDependencyStructure() {
        return dependencyService.deriveStructure();
    }

    // The data model endpoint generates a structure of entity objects and their member variables along with the
    // annotation values on those member variables
    @RequestMapping(value = "/dataModel")
    @GetMapping
    public String generateDataModelStructure() {
        return dataModelService.deriveStructure();
    }

    // The security endpoint generates a list for each role of what methods may be called by the role specified
    @RequestMapping(value = "/security")
    @GetMapping
    public String generateSecurityStructure() {
        return securityService.deriveStructure();
    }

}
