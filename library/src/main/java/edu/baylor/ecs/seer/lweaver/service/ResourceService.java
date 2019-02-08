package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.common.context.SeerMsContext;
import edu.baylor.ecs.seer.common.context.SeerRequestContext;
import javassist.CtClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javassist.*;
import javassist.bytecode.*;
@Service
public class ResourceService {

    @Autowired
    protected ResourceLoader resourceLoader

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
        String directory = new File(folderPath).getAbsolutePath();
        Path start = Paths.get(directory);
        int maxDepth = 15;
        List<String> resourcePaths = new ArrayList<>();
        try {
            Stream<Path> stream = Files.find(start, maxDepth, (path, attr) -> String.valueOf(path).toLowerCase().endsWith(".jar") || String.valueOf(path).toLowerCase().endsWith(".war"));
            resourcePaths = stream
                    .sorted()
                    .map(String::valueOf)
                    .filter((path) -> {
                        return (String.valueOf(path).toLowerCase().endsWith(".jar") ||
                                String.valueOf(path).toLowerCase().endsWith(".war")) &&
                                !String.valueOf(path).toLowerCase().contains("/.mvn/") &&
                                !String.valueOf(path).toLowerCase().startsWith("/usr/lib/jvm/") &&
                                !String.valueOf(path).toLowerCase().contains("/target/dependency/") &&
                                !String.valueOf(path).toLowerCase().contains("/gradle");
                    })
                    .collect(Collectors.toList());
        } catch(Exception e){
            e.printStackTrace();
        }
        return resourcePaths;
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
        Resource classPathResource = resourceLoader.getResource("file:" + file);



    }
}
