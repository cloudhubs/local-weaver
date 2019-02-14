package edu.baylor.ecs.seer.lweaver.resource;

import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.lweaver.service.SeerContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/local-weaver")
public class LocalWeaverResource {

    @Autowired
    private SeerContextService seerContextService;

    @PostMapping
    @RequestMapping(value = "/")
    public SeerContext generateSeerContext(@RequestBody SeerContext context) {
        return seerContextService.populateSeerContext(context);
    }

}
