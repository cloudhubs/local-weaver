package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.common.context.SeerMsContext;
import edu.baylor.ecs.seer.common.context.SeerRequestContext;
import javassist.CtClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResourceService {

    @Autowired
    private SeerMsSecurityContextService securityService;

    @Autowired SeerMsEntityContextService entityService;

    public SeerContext getResources(SeerContext seerContext){
        SeerRequestContext req = seerContext.getRequest();
        List<String> resourcePaths = getResourcePaths(req.getPathToCompiledMicroservices());
        List<SeerMsContext> msContexts = generateMsContexts(resourcePaths);
        seerContext.setMsContexts(msContexts);
        return seerContext;
    }

    private List<String> getResourcePaths(String folderPath){
        //ToDo:
        return null;
    }

    private List<SeerMsContext> generateMsContexts(List<String> resourcePaths){
        List<SeerMsContext> msContexts = new ArrayList<>();
        for (String path: resourcePaths
             ) {
            SeerMsContext msContext = new SeerMsContext();
            List<CtClass> msCtClasses = getMsCtClasses(path);
            msContext.setCtClassesResourcePool(msCtClasses);
            msContext.setEntity(entityService.getSeerEntityContext(msCtClasses));
            msContext.setSecurity(securityService.getMsSeerSecurityContext(msCtClasses));
            msContexts.add(msContext);
        }
        return msContexts;
    }

    private List<CtClass> getMsCtClasses(String resourcePath){
        //ToDo:
        return null;
    }
}
