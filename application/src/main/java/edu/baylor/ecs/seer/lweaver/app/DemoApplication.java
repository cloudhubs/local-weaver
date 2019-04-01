package edu.baylor.ecs.seer.lweaver.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "edu.baylor.ecs.seer")

public class DemoApplication {




    public DemoApplication(){
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }


}
