package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.context.SeerSecurityContext;
import javassist.CtClass;
import javassist.bytecode.ClassFile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class SeerMsSecurityContextService {

    public SeerSecurityContext getMsSeerSecurityContext(Set<ClassFile> msClassFiles){
        return null;
    }

}
