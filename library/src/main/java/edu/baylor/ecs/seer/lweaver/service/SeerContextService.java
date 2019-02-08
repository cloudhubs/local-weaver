package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.common.context.SeerMsContext;
import edu.baylor.ecs.seer.common.context.SeerRequestContext;
import edu.baylor.ecs.seer.common.context.SeerSecurityContext;
import javassist.CtClass;
import javassist.bytecode.ClassFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class SeerContextService {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private SeerMsSecurityContextService securityService;

    @Autowired
    private SeerMsEntityContextService entityService;

    private List<CtClass> allCtClasses;

    public SeerContext populateSeerContext(SeerContext seerContext){
        SeerRequestContext req = seerContext.getRequest();
        List<String> resourcePaths = null;
        if (req.isUseRemote()){
            resourcePaths = resourceService.getResourcePaths("");
        } else {
            resourcePaths = resourceService.getResourcePaths(req.getPathToCompiledMicroservices());
        }
        List<SeerMsContext> msContexts = generateMsContexts(resourcePaths);
        seerContext.setMsContexts(msContexts);
        seerContext.setSecurity(generateSecurityContext(seerContext.getSecurity()));
        return seerContext;
    }

    private List<SeerMsContext> generateMsContexts(List<String> resourcePaths){
        List<SeerMsContext> msContexts = new ArrayList<>();
        allCtClasses = new ArrayList<>();
        for (String path: resourcePaths
        ) {
            SeerMsContext msContext = new SeerMsContext();
            List<CtClass> ctClasses = resourceService.getCtClasses(path);
            allCtClasses.addAll(ctClasses);
            msContext.setEntity(entityService.getSeerEntityContext(ctClasses));
            msContexts.add(msContext);
        }
        return msContexts;
    }

    private SeerSecurityContext generateSecurityContext(SeerSecurityContext seerSecurityContext){
        return securityService.getMsSeerSecurityContext(this.allCtClasses, seerSecurityContext);
    }
}
