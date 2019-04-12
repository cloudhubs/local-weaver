package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.api.SeerApiMethod;
import edu.baylor.ecs.seer.common.api.SeerApiType;
import edu.baylor.ecs.seer.common.context.SeerApiContext;
import edu.baylor.ecs.seer.common.entity.EntityModel;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeerMsApiContextService {

    public SeerApiContext createSeerApiContext(List<CtClass> ctClassesApiIn){
        List<SeerApiMethod> apiMethods = new ArrayList<>();
        for (CtClass ctClass: ctClassesApiIn){
            CtMethod[] ctMethods = ctClass.getMethods();
            for (CtMethod ctMethod: ctMethods
                 ) {
                apiMethods.add(createSeerApiMethod(ctClass, ctMethod));
            }
        }
        SeerApiContext seerApiContext = new SeerApiContext();
        return seerApiContext;
    }

    public SeerApiMethod createSeerApiMethod(CtClass ctClass, CtMethod ctMethod){
        SeerApiMethod seerApiMethod = new SeerApiMethod();
        seerApiMethod.setClassName(ctClass.getName());
        seerApiMethod.setMethodName(ctMethod.getLongName());
        seerApiMethod.setSeerApiType(getSeerApiType(ctMethod));
        if (seerApiMethod.getSeerApiType().equals(SeerApiType.OUT)){
            seerApiMethod.setEntityModel(getReturnType(ctMethod));
        } else {
            seerApiMethod.setEntityModel(getParameterType(ctMethod));
        }
        return seerApiMethod;
    }

    private EntityModel getParameterType(CtMethod ctMethod) {
        EntityModel entityModel = new EntityModel();
        try {
            CtClass[] parameterTypes = ctMethod.getParameterTypes();
            for (CtClass p: parameterTypes) {
                System.out.println(p.getName());
                entityModel.setClassNameShort(p.getSimpleName());
                entityModel.setClassName(p.getName());
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return entityModel;
    }

    private EntityModel getReturnType(CtMethod ctMethod) {
        EntityModel entityModel = new EntityModel();
        try {
            CtClass ctClass = ctMethod.getReturnType();
            entityModel.setClassNameShort(ctClass.getSimpleName());
            entityModel.setClassName(ctClass.getName());
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return entityModel;
    }


    private SeerApiType getSeerApiType(CtMethod ctMethod) {
        Object[] annotations = new Object[]{};
        SeerApiType seerApiType = SeerApiType.IN;
        try {
            annotations = ctMethod.getAnnotations();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        for (Object annotation: annotations
             ) {
            if (annotation.getClass().getName().equals("javax.ws.rs.GET")){
                seerApiType = SeerApiType.OUT;
            } else if (annotation.getClass().getName().equals("javax.ws.rs.POST")){
                seerApiType = SeerApiType.IN;
            }
        }
        return seerApiType;
    }


}
