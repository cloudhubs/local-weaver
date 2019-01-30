package edu.baylor.ecs.seer.lweaver.api;

import edu.baylor.ecs.seer.lweaver.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
<<<<<<< Updated upstream
=======
import org.springframework.web.bind.annotation.PathVariable;
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
=======
    private String validatePath(String arg) {
        if (arg.endsWith("~!r!~")) {
            arg = arg.substring(0, arg.length() - 5);
            arg = arg.replaceAll("~", "/");
            arg = arg.replaceAll("\\^", "..");
        }
        return arg;
    }

>>>>>>> Stashed changes
    @Autowired
    public LocalWeaverApi(FlowStructureService flowStructureService, BytecodeFlowStructureService bytecodeFlowStructureService, DependencyService dependencyService, DataModelService dataModelService, SecurityService securityService) {
        this.flowStructureService = flowStructureService;
        this.bytecodeFlowStructureService = bytecodeFlowStructureService;
        this.dependencyService = dependencyService;
        this.dataModelService = dataModelService;
        this.securityService = securityService;
    }

<<<<<<< Updated upstream
    // The flow structure endpoint generates a structure showing what methods are called by what classes and what
    // methods those methods call
    @RequestMapping(value = "/flowStructure")
    @GetMapping
    public String generateFlowStructure() {
        return flowStructureService.deriveStructure();
=======
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
>>>>>>> Stashed changes
    }

    // The bytecode flow structure endpoint generates a structure of bytecode instructions as a tree, filtering out
    // the unnecessary ones to be used to analyze if and for cycles
<<<<<<< Updated upstream
    @RequestMapping(value = "/bytecodeFlowStructure")
    @GetMapping
    public String generateBytecodeFlowStructure() {
        return bytecodeFlowStructureService.deriveStructure();
=======
    @RequestMapping(value = "/bytecodeFlowStructure/{path}")
    @GetMapping
    public String generateBytecodeFlowStructure(@PathVariable String path) {
        return bytecodeFlowStructureService.deriveStructure(validatePath(path));
>>>>>>> Stashed changes
    }

    // The dependency endpoint generates a list of what outside packages are used and how many times they are used in
    // the application
<<<<<<< Updated upstream
    @RequestMapping(value = "/dependency")
    @GetMapping
    public String generateDependencyStructure() {
        return dependencyService.deriveStructure();
=======
    @RequestMapping(value = "/dependency/{path}")
    @GetMapping
    public String generateDependencyStructure(@PathVariable String path) {
        return dependencyService.deriveStructure(validatePath(path));
>>>>>>> Stashed changes
    }

    // The data model endpoint generates a structure of entity objects and their member variables along with the
    // annotation values on those member variables
<<<<<<< Updated upstream
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
=======
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
>>>>>>> Stashed changes
    }

}
