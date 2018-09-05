package edu.baylor.ecs.cfgg.loader.api;

import edu.baylor.ecs.cfgg.loader.service.EvaluatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;

@RestController
@RequestMapping("/loader")
public class EvaluatorApi {

    @Autowired
    private EvaluatorService evaluatorService;

    @ResponseBody
    @RequestMapping(value = "/getClasses")
    @GetMapping
    public ArrayList<Class<?>> getClasses() {

        evaluatorService.getClassesFromJar();

        ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
        classList.add(SampleClass.class);
        classList.add(SampleClass.class);
        classList.add(SampleClass.class);
        classList.add(SampleClass.class);

        return classList;

    }

    public class SampleClass {

        public SampleClass(){}
    }
}
