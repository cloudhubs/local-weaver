package edu.baylor.ecs.cfgg.processor.api;

import edu.baylor.ecs.cfgg.processor.service.CouplingCohesionProcessorService;
import edu.baylor.ecs.cfgg.processor.service.ProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;


@RestController
@RequestMapping("/processor")
public class GeneratorApi {

    @Autowired
    private CouplingCohesionProcessorService processorService;

    @RequestMapping(value = "/sourceCode")
    @GetMapping
    public String returnSourceCode() throws IOException, URISyntaxException {
        return processorService.generateSourceCode();

    }

}
