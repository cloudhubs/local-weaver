package edu.baylor.ecs.seer.lweaver.api;

import edu.baylor.ecs.seer.lweaver.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    private String validatePath(String arg) {
        if (arg.endsWith("~!r!~")) {
            arg = arg.substring(0, arg.length() - 5);
            arg = arg.replaceAll("~", "/");
            arg = arg.replaceAll("\\^", "..");
        }
        return arg;
    }

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
    @RequestMapping(value = "/home/{arg}")
    @GetMapping
    public String home(@PathVariable String arg) {
        return "Welcome " + validatePath(arg) + "!";
    }

    // The flow structure endpoint generates a structure showing what methods are called by what classes and what
    // methods those methods call
    @RequestMapping(value = "/flowStructure/{path}")
    @GetMapping
    public String generateFlowStructure(@PathVariable String path) {
        return flowStructureService.deriveStructure(validatePath(path));
    }

    // The bytecode flow structure endpoint generates a structure of bytecode instructions as a tree, filtering out
    // the unnecessary ones to be used to analyze if and for cycles


    @RequestMapping(value = "/bytecodeFlowStructure/{path}")
    @GetMapping
    public String generateBytecodeFlowStructure(@PathVariable String path) {
        return bytecodeFlowStructureService.deriveStructure(validatePath(path));

    }

    // The dependency endpoint generates a list of what outside packages are used and how many times they are used in
    // the application

    @RequestMapping(value = "/dependency/{path}")
    @GetMapping
    public String generateDependencyStructure(@PathVariable String path) {
        return dependencyService.deriveStructure(validatePath(path));

    }

    // The data model endpoint generates a structure of entity objects and their member variables along with the
    // annotation values on those member variables

    @RequestMapping(value = "/dataModel/{path}")
    @GetMapping
    public String generateDataModelStructure(@PathVariable String path) {
        return dataModelService.deriveStructure(validatePath(path));
    }

    // The security endpoint generates a list for each role of what methods may be called by the role specified
    @RequestMapping(value = "/security/{path}")
    @GetMapping
    public String generateSecurityStructure(@PathVariable String path) {
        return securityService.deriveStructure(validatePath(path));
    }

}
