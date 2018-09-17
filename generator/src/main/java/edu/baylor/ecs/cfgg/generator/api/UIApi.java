package edu.baylor.ecs.cfgg.generator.api;

import edu.baylor.ecs.cfgg.generator.service.GeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/generator")
public class UIApi {

    @Autowired
    private GeneratorService generatorService;

    @RequestMapping(value = "/graph")
    @GetMapping
    public List<String> getGraph() {
        List<String> graph = null;
        try {
            graph = generatorService.generateGraph();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // graph picture
        return graph;

    }

}
