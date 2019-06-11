package edu.baylor.ecs.seer.lweaver.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.baylor.ecs.seer.common.SampleObject;
import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.lweaver.service.SeerContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class Controller {

    @Autowired
    private SeerContextService seerContextService;

//    @Autowired
//    private JavassistClassService javassistClassService;
    @CrossOrigin(origins = "*")
    @RequestMapping(path = "/", method = RequestMethod.POST, produces = "application/json; charset=UTF-8", consumes = {"text/plain", "application/*"})
    @ResponseBody
    public String generateSeerContext(@RequestBody SampleObject sampleObject) {
        SeerContext context = new SeerContext();
        context.setRequest(sampleObject.getRequest());
        context = seerContextService.populateSeerContext(context);

        ObjectMapper objectMapper = new ObjectMapper();

        String json = null;
        try {
            json = objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

//    @RequestMapping(path = "/", method = RequestMethod.POST, produces = "application/json; charset=UTF-8", consumes = {"text/plain", "application/*"})
//    @ResponseBody
//    public String handshake(@RequestBody SampleObject context){
//        return "string";
//    }


//    @GetMapping
//    @RequestMapping(value = "/class")
//    public void getClasses(){
//        javassistClassService.classPool();
//    }
}
