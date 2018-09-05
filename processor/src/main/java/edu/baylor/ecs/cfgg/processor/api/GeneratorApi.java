package edu.baylor.ecs.cfgg.processor.api;

import edu.baylor.ecs.cfgg.processor.service.ProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/processor")
public class GeneratorApi {

    @Autowired
    private ProcessorService processorService;

    @RequestMapping(value = "/sourceCode")
    @GetMapping
    public String returnSourceCode() {
        String sourceCode = processorService.generateSourceCode();
        //return graph source code in dot
        return sourceCode;

    }

}
