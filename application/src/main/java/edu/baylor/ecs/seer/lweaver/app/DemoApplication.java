package edu.baylor.ecs.seer.lweaver.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This class is the {@link SpringBootApplication} runner for the demo application
 * for the local-weaver
 *
 * @author  Jan Svacina
 * @version 1.0
 * @since   0.3.0
 */
@SpringBootApplication(scanBasePackages = "edu.baylor.ecs.seer")
public class DemoApplication {

    /**
     * Empty constructor for {@link DemoApplication}
     */
    public DemoApplication(){
        // Empty constructor
    }

    /**
     * This main method runs the {@link DemoApplication} {@link SpringBootApplication}
     */
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }


}
