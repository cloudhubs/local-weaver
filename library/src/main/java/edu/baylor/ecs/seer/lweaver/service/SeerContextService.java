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

    @Autowired
    private FlowStructureService flowService;

    @Autowired
    private BytecodeFlowStructureService byteCode;

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
        for (String path: resourcePaths
        ) {
            SeerMsContext msContext = new SeerMsContext();
            msContext.setModuleName(path.substring(path.lastIndexOf('/') + 1));
            List<CtClass> ctClasses = resourceService.getCtClasses(path, organizationPath);
            allCtClasses.addAll(ctClasses);
            msContext.setCtClasses(ctClasses);
            msContext.setEntity(entityService.getSeerEntityContext(ctClasses));
            msContexts.add(msContext);
            flowService.process(ctClasses, new SeerContext());
            byteCode.process(ctClasses);
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
