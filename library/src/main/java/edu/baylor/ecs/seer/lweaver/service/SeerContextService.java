package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.context.*;
import edu.baylor.ecs.seer.common.entity.EntityModel;
import javassist.CtClass;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * The SeerContextService service populates a {@link SeerContext}. The entry point for this
 * class is the {@link SeerContextService#populateSeerContext(SeerContext)}.
 *
 * </p>
 *
 * This service depends on {@link ResourceService}, {@link SeerMsSecurityContextService}, {@link SeerMsEntityContextService},
 * {@link FlowStructureService}, {@link BytecodeFlowStructureService}, {@link SeerMsApiContextService} services
 * to construct the various aspects of the {@link SeerContext}.
 *
 * @author  Jan Svacina
 * @version 1.3
 * @since   0.3.0
 */
@Service
public class SeerContextService {

    // Service for managing the resources in the project
    private final ResourceService resourceService;

    // Service for managing the security aspect of the microservices
    private final SeerMsSecurityContextService securityService;

    // Service for managing the entity aspect of the microservices
    private final SeerMsEntityContextService entityService;

    // Service for managing the flow aspect of the microservices
    private final FlowStructureService flowService;

    // Service for managing the bytecode flow aspect of the microservices
    private final BytecodeFlowStructureService bytecodeService;

    // Service for managing the api aspect of the microservices
    private final SeerMsApiContextService apiSerivce;

    // List of CtClasses for constructing the SeerSecurityContext
    private List<CtClass> allCtClasses;

    /**
     * Constructor for {@link SeerContextService} which injects the needed services
     *
     * @param resourceService service for managing the resources in the project
     * @param securityService service for managing the security aspect of the microservices
     * @param entityService service for managing the entity aspect of the microservices
     * @param flowService service for managing the flow aspect of the microservices
     * @param bytecodeService service for managing the bytecode flow aspect of the microservices
     * @param apiSerivce service for managing the api aspect of the microservices
     */
    public SeerContextService(ResourceService resourceService, SeerMsSecurityContextService securityService, SeerMsEntityContextService entityService, FlowStructureService flowService, BytecodeFlowStructureService bytecodeService, SeerMsApiContextService apiSerivce) {
        this.resourceService = resourceService;
        this.securityService = securityService;
        this.entityService = entityService;
        this.flowService = flowService;
        this.bytecodeService = bytecodeService;
        this.apiSerivce = apiSerivce;
    }

    /**
     * This method returns a {@link SeerContext} populated with {@link SeerMsContext} objects
     * for each of the microservices in the project. This method is the entry point for
     * {@link SeerContextService}.
     *
     * @param seerContext the {@link SeerContext} holding the {@link SeerRequestContext}
     *
     * @return a {@link SeerContext} populated with {@link SeerMsContext} objects
     */
    public SeerContext populateSeerContext(SeerContext seerContext){
        SeerRequestContext req = seerContext.getRequest();
        List<String> resourcePaths = getResourcePaths(req);
        List<SeerMsContext> msContexts = generateMsContexts(resourcePaths, req);
        seerContext.setMsContexts(msContexts);
        //SeerSecurityContext seerSecurityContext = generateSecurityContext(seerContext.getRequest());
        //seerContext.setSecurity(seerSecurityContext);
        return seerContext;
    }

    /**
     * This method returns a {@link List} of {@link String} objects, each of which is a JAR
     * file for a microservice. This is a private helper method called by
     * {@link SeerContextService#populateSeerContext(SeerContext)}
     *
     * @param req the {@link SeerRequestContext} containing the path to the project to analyze
     *
     * @return a {@link List} of {@link String} objects, each of which is a JAR file for
     * a microservice
     */
    private List<String> getResourcePaths(SeerRequestContext req){
        List<String> resourcePaths = null;
        if (req.isUseRemote()){
            resourcePaths = resourceService.getResourcePaths("");
        } else {
            resourcePaths = resourceService.getResourcePaths(req.getPathToCompiledMicroservices());
        }
        return resourcePaths;
    }

    /**
     * This method returns a {@link List} of {@link SeerMsContext} objects. Each {@link SeerMsContext}
     * holds the representation of a microservice in the project. This is a private helper method
     * called by {@link SeerContextService#populateSeerContext(SeerContext)}.
     *
     * @param resourcePaths a {@link List} of paths to the compiled JAR files
     * @param request the {@link SeerRequestContext} with necessary metadata
     *
     * @return a {@link List} of {@link SeerMsContext} objects representing the microservices
     * from the project
     */
    private List<SeerMsContext> generateMsContexts(List<String> resourcePaths, SeerRequestContext request){
        String organizationPath = request.getOrganizationPath();
        List<SeerMsContext> msContexts = new ArrayList<>();
        allCtClasses = new ArrayList<>();
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");

        for (String path: resourcePaths) {
            SeerMsContext msContext = new SeerMsContext();
            System.out.println(path);
            int lastIndex = path.lastIndexOf('.');
            if(isWindows) {
                msContext.setModuleName(path.substring(path.lastIndexOf('\\') + 1, lastIndex));
            } else {
                msContext.setModuleName(path.substring(path.lastIndexOf('/') + 1, lastIndex));
            }
            List<CtClass> ctClasses = resourceService.getCtClasses(path, organizationPath);
            allCtClasses.addAll(ctClasses);

            Set<Properties> properties = resourceService.getProperties(path, organizationPath);
            if (properties.size() > 0){
                Properties prop = properties.iterator().next();
                String port = prop.getProperty("port");
                if(port == null){
                    port = prop.getProperty("server.port");
                }
                msContext.setPort(Integer.parseInt(port));
            }

            msContext.setEntity(entityService.getSeerEntityContext(ctClasses));
            for (EntityModel e: msContext.getEntity().getEntities()) {
                e.setModuleName(msContext.getModuleName());
            }

            if (msContext.getEntity().getEntities().size() > 0){

                SeerFlowContext seerFlowContext = flowService.process(ctClasses);
                bytecodeService.process(seerFlowContext);
                msContext.setFlow(seerFlowContext);

                SeerApiContext seerApiContext = apiSerivce.createSeerApiContext(ctClasses);
                msContext.setApi(seerApiContext);
            }

            //SeerSecurityContext seerSecurityContext = securityService.getMsSeerSecurityContext(ctClasses, request);
            //msContext.setSecurity(seerSecurityContext);

            msContexts.add(msContext);

        }
        return msContexts;
    }

    /**
     * This method returns a {@link SeerSecurityContext} from a given {@link SeerRequestContext}.
     * A {@link List} of all {@link CtClass} objects from the entire system is required. This list
     * is created during {@link SeerContextService#generateMsContexts(List, SeerRequestContext)} .
     *
     * @param req the {@link SeerRequestContext} containing the path to the project to analyze
     *
     * @return a {@link SeerSecurityContext} from a given {@link SeerRequestContext}
     */
    private SeerSecurityContext generateSecurityContext(SeerRequestContext req){
        return securityService.getMsSeerSecurityContext(this.allCtClasses, req);
    }
}
