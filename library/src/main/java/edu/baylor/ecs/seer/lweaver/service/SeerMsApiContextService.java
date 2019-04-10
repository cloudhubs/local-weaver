package edu.baylor.ecs.seer.lweaver.service;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.util.List;

public class SeerMsApiContextService {


    public void getMethods(List<CtClass> ctClassesApiIn){
        for (CtClass ctClass: ctClassesApiIn){
            CtMethod[] ctMethods = ctClass.getMethods();
            for (CtMethod ctMethod: ctMethods
                 ) {
                //Parameter Types
                try {
                    ctMethod.getParameterTypes();
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
                //
                ctMethod.getReturnType();

            }
        }
    }
}
