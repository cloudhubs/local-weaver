package edu.baylor.ecs.seer.lweaver.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.common.context.SeerFlowContext;
import edu.baylor.ecs.seer.common.context.SeerMsContext;
import edu.baylor.ecs.seer.lweaver.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class Controller {

    @Autowired
    private SeerContextService seerContextService;

    @Autowired
    private JavassistClassService javassistClassService;

    @Autowired
    private BytecodeFlowStructureService bytecodeFlowStructureService;


    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public SeerContext generateSeerContext(@RequestBody SeerContext context) {
        context = seerContextService.populateSeerContext(context);
        return context;
    }


    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/analyze", method = RequestMethod.POST)
    public SeerContext analyzeBytecode(@RequestBody SeerContext context) throws JsonProcessingException {
        context = generateSeerContext(context);
        for (SeerMsContext msContext : context.getMsContexts()) {
            SeerFlowContext flowContext = new SeerFlowContext();
            flowContext.setMethodMaps(bytecodeFlowStructureService.process(msContext.getCtClasses()));
            msContext.setFlow(flowContext);
        }
        return context;
//        return new ObjectMapper().writer().writeValueAsString(context);
    }


    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/class", method = RequestMethod.GET)
    public void getClasses(){
        javassistClassService.classPool();
    }
}
