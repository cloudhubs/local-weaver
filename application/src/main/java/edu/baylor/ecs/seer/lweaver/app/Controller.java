package edu.baylor.ecs.seer.lweaver.app;

import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.lweaver.service.JavassistClassService;
import edu.baylor.ecs.seer.lweaver.service.SeerContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class Controller {

    @Autowired
    private SeerContextService seerContextService;

    @Autowired
    private JavassistClassService javassistClassService;

    @PostMapping
    @RequestMapping(value = "/")
    public SeerContext generateSeerContext(@RequestBody SeerContext context) {
        context = seerContextService.populateSeerContext(context);
        return context;
    }


    @GetMapping
    @RequestMapping(value = "/class")
    public void getClasses(){
        javassistClassService.classPool();
    }
}
