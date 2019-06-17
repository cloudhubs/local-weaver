package edu.baylor.ecs.seer.lweaver.app;

import edu.baylor.ecs.seer.common.SampleObject;
import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.lweaver.service.SeerContextService;
import org.springframework.web.bind.annotation.*;

/**
 * This class is the {@link RestController} for the demo application for the local-weaver
 *
 * @author  Jan Svacina
 * @version 1.0
 * @since   0.3.0
 */
@RestController
public class Controller {

    // Service in charge of constructing the SeerContext
    private final SeerContextService seerContextService;

    /**
     * Constructor for {@link Controller} which injects a {@link SeerContextService}
     * @param seerContextService a service for constructing the {@link SeerContext}
     */
    public Controller(SeerContextService seerContextService){
        this.seerContextService = seerContextService;
    }

    /**
     * This method establishes an endpoint for verifying the service is still running
     *
     * @return a string greeting denoting the service is running
     */
    @RequestMapping(path = "/handshake", method = RequestMethod.GET, produces = "application/json; charset=UTF-8", consumes = {"text/plain", "application/*"})
    public String handshake(){
        return "Hello from local-weaver demo Controller";
    }

    /**
     * Returns a {@link edu.baylor.ecs.seer.common.context.SeerContext} that represents all
     * microservices found by the {@link SeerContextService}
     *
     * @param  sampleObject  a wrapper class for the {@link edu.baylor.ecs.seer.common.context.SeerRequestContext}
     *                       which contains the configuration information for analyzing the
     *                       microservices
     *
     * @return the {@link SeerContext} {@link SeerContext} representing all the microservices
     */
    @CrossOrigin(origins = "*")
    @RequestMapping(path = "/", method = RequestMethod.POST, produces = "application/json; charset=UTF-8", consumes = {"text/plain", "application/*"})
    @ResponseBody
    public SeerContext generateSeerContext(@RequestBody SampleObject sampleObject) {
        // Initialize a new SeerContext with the request
        SeerContext context = new SeerContext();
        context.setRequest(sampleObject.getRequest());

        // Generate the full SeerContext
        context = seerContextService.populateSeerContext(context);
        return context;
    }
}
