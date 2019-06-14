package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.context.*;
import edu.baylor.ecs.seer.common.entity.EntityModel;
import javassist.CtClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SeerContextService {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private SeerMsSecurityContextService securityService;

    @Autowired
    private SeerMsEntityContextService entityService;

    @Autowired
    private FlowStructureService flowService;

    @Autowired
    private BytecodeFlowStructureService bytecodeService;

    @Autowired
    private SeerMsApiContextService apiSerivce;

    private List<CtClass> allCtClasses;

    public SeerContext populateSeerContext(SeerContext seerContext){
        SeerRequestContext req = seerContext.getRequest();
        List<String> resourcePaths = getResourcePaths(req);
        List<SeerMsContext> msContexts = generateMsContexts(resourcePaths, req.getOrganizationPath());
        seerContext.setMsContexts(msContexts);
        //seerContext.setSecurity(generateSecurityContext(req));
        return seerContext;
    }

    private List<String> getResourcePaths(SeerRequestContext req){
        List<String> resourcePaths = null;
        if (req.isUseRemote()){
            resourcePaths = resourceService.getResourcePaths("");
        } else {
            resourcePaths = resourceService.getResourcePaths(req.getPathToCompiledMicroservices());
        }
        return resourcePaths;
    }

    private List<SeerMsContext> generateMsContexts(List<String> resourcePaths, String organizationPath){
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

            //entities
            msContext.setEntity(entityService.getSeerEntityContext(ctClasses));
            for (EntityModel e: msContext.getEntity().getEntities()
            ) {
                e.setModuleName(msContext.getModuleName());
            }
            //API

            //getting rid of wars from java libraries
            if (msContext.getEntity().getEntities().size() > 0){

                SeerFlowContext seerFlowContext = flowService.process(ctClasses);
                bytecodeService.process(seerFlowContext);
                msContext.setFlow(seerFlowContext);

                SeerApiContext seerApiContext = apiSerivce.createSeerApiContext(ctClasses);
                msContext.setApi(seerApiContext);

            }
            msContexts.add(msContext);
            //flows

//            byteCode.process(ctClasses);
            //API


        }
        return msContexts;
    }


    private SeerSecurityContext generateSecurityContext(SeerRequestContext req){
        return securityService.getMsSeerSecurityContext(this.allCtClasses, req);
    }

    //set module characteristics

    //set module lines of code

    //set module entities counter

    //set module entities context

    //set global context


}
