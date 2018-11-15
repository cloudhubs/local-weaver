package edu.baylor.ecs.seer.lweaver.aspect;

import org.aspectj.lang.annotation.Pointcut;

public class CommonJoinPointConfig {

    @Pointcut("execution(* edu.baylor.ecs.seer.lweaver.api.LocalWeaverApi.*(..))")
    public void apiCall(){}

    @Pointcut("execution(* edu.baylor.ecs.seer.lweaver.service.BytecodeFlowStructureService.filter(..))")
    public void bytecodeServiceFilter(){}

    @Pointcut("execution(* edu.baylor.ecs.seer.lweaver.service.BytecodeFlowStructureService.process(..))")
    public void bytecodeServiceProcess(){}

    @Pointcut("execution(* edu.baylor.ecs.seer.lweaver.service.BytecodeFlowStructureService.preprocessBytecode(..))")
    public void bytecodeServicePreprocessBytecode(){}

    @Pointcut("execution(* edu.baylor.ecs.seer.lweaver.service.BytecodeFlowStructureService.processBytecode(..))")
    public void bytecodeServiceProcessBytecode(){}

    @Pointcut("execution(* edu.baylor.ecs.seer.lweaver.service.BytecodeFlowStructureService.postProcessBytecode(..))")
    public void bytecodeServicePostProcessBytecode(){}

    /*@Pointcut("execution(* edu.baylor.ecs.seer.lweaver.service.BytecodeFlowStructureService.*(..))")
    public void bytecodeServiceCall(){}*/
}
