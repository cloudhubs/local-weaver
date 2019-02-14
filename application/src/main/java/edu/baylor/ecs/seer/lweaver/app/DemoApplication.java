package edu.baylor.ecs.seer.lweaver.app;

import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.lweaver.service.SeerContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = "edu.baylor.ecs.seer")

public class DemoApplication {




    public DemoApplication(){
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }


}
