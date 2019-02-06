package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.common.context.SeerMsContext;
import edu.baylor.ecs.seer.common.context.SeerRequestContext;
import javassist.CtClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javassist.*;
import javassist.bytecode.*;
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
            Set<ClassFile> msClassFiles = getMsClassFiles(path);
            msContext.setEntity(entityService.getSeerEntityContext(msClassFiles));
            msContext.setSecurity(securityService.getMsSeerSecurityContext(msClassFiles));
            msContexts.add(msContext);
        }
        return msContexts;
    }



    private Set<ClassFile> getMsClassFiles(String path){
        //ToDo: Get Resource
    }
}
