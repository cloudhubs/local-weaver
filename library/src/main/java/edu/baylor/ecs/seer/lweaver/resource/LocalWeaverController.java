package edu.baylor.ecs.seer.lweaver.resource;

import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.lweaver.service.SeerContextService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class is the {@link RestController} for the local-weaver library. It exposes an endpoint
 * which will construct a SeerContext
 *
 * @author Jan Svacina
 * @version 1.0
 * @since 0.3.0
 */
@RestController
@RequestMapping("/local-weaver")
public class LocalWeaverController {

  // Service in charge of constructing the SeerContext
  private final SeerContextService seerContextService;

  /**
   * Constructor for {@link LocalWeaverController} which injects a {@link SeerContextService}
   *
   * @param seerContextService a service for constructing the {@link SeerContext}
   */
  public LocalWeaverController(SeerContextService seerContextService) {
    this.seerContextService = seerContextService;
  }

  /**
   * This method establishes an endpoint for verifying the service is still running
   *
   * @return a string greeting denoting the service is running
   */
  @RequestMapping(path = "/handshake", method = RequestMethod.GET, produces = "application/json; charset=UTF-8",
    consumes = {"text/plain", "application/*"})
  public String handshake() {
    return "Hello from local-weaver library Controller";
  }

  /**
   * This method returns a {@link edu.baylor.ecs.seer.common.context.SeerContext} that represents all
   * microservices found by the {@link SeerContextService}
   *
   * @param context a {@link SeerContext} object to be populated by the {@link SeerContextService}
   * @return the {@link SeerContext} representing all the microservices
   */
  @PostMapping
  @RequestMapping(value = "/")
  public SeerContext generateSeerContext(@RequestBody SeerContext context) {
    return seerContextService.populateSeerContext(context);
  }

}
