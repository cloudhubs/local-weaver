package edu.baylor.ecs.cfgg.processor;

import edu.baylor.ecs.cfgg.processor.service.CouplingCohesionProcessorService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProcessorApplication.class, args);
    }
}
