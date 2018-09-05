package edu.baylor.ecs.cfgg.generator.api;

import edu.baylor.ecs.cfgg.generator.service.GeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/generator")
public class UIApi {

    @Autowired
    private GeneratorService generatorService;

    @RequestMapping(value = "/graph")
    @GetMapping
    public String getGraph() {
        String graph = generatorService.generateGraph();
        // graph picture
        return graph;

    }

}
