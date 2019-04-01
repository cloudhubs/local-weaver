package edu.baylor.ecs.seer.lweaver.app;

import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.common.context.SeerMsContext;
import edu.baylor.ecs.seer.lweaver.service.*;
import javassist.CtClass;
import javassist.bytecode.ClassFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
public class Controller {

    @Autowired
    private SeerContextService seerContextService;

    @Autowired
    private JavassistClassService javassistClassService;

    @Autowired
    private BytecodeFlowStructureService bytecodeFlowStructureService;

    @Autowired
    private ResourceService resourceService;

    @PostMapping
    @RequestMapping(value = "/")
    public SeerContext generateSeerContext(@RequestBody SeerContext context) {
        context = seerContextService.populateSeerContext(context);
        return context;
    }


    @PostMapping
    @RequestMapping(value = "/analyze")
    public SeerContext analyzeBytecode(@RequestBody SeerContext context) {
        context = generateSeerContext(context);
        for (SeerMsContext msContext : context.getMsContexts()) {
            bytecodeFlowStructureService.process(msContext.getCtClasses());
        }
        return context;
    }


    @GetMapping
    @RequestMapping(value = "/class")
    public void getClasses(){
        javassistClassService.classPool();
    }
}
