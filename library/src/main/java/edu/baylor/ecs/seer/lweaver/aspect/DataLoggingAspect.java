package edu.baylor.ecs.seer.lweaver.aspect;

import edu.baylor.ecs.seer.common.FlowNode;
import javassist.CtClass;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Aspect
@Configuration
@Configurable
public class DataLoggingAspect {

    private Logger logger = LoggerFactory.getLogger(DataLoggingAspect.class);
    private Integer numGraphs;
    private Integer numProcessed;

    @Around(value="edu.baylor.ecs.seer.lweaver.aspect.CommonJoinPointConfig.apiCall()")
    public Object apiCallAround(ProceedingJoinPoint joinPoint) throws Throwable {
        resetLogger(joinPoint);
        String name = joinPoint.getSignature().getName();
        logger.info("[Recieved call for " + name + " ... ]");
        Object o = joinPoint.proceed();
        resetLogger(joinPoint);
        logger.info("[Finished processing " + name + ".]");
        return o;
    }

    @Around(value="edu.baylor.ecs.seer.lweaver.aspect.CommonJoinPointConfig.bytecodeServiceProcess()")
    public Object bytecodeServiceProcessAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String name = joinPoint.getSignature().getName();
        resetLogger(joinPoint);
        logger.info(name + " [Processing module ... ]");
        Object o = joinPoint.proceed();
        resetLogger(joinPoint);
        logger.info(name + " [Finished processing module.]");
        return o;
    }

    @Around(value="edu.baylor.ecs.seer.lweaver.aspect.CommonJoinPointConfig.bytecodeServicePreprocessBytecode()")
    public Object bytecodeServicePreprocessBytecode(ProceedingJoinPoint joinPoint) throws Throwable {
        String name = joinPoint.getSignature().getName();
        resetLogger(joinPoint);
        logger.info(name + " [Preprocessing bytecode into individual methods ... ]");
        Object o = joinPoint.proceed();
        if(o instanceof List<?>){
            List<String> genericList = (List<String>)o;
            numGraphs = genericList.size();
            numProcessed = 0;
            resetLogger(joinPoint);
            logger.info(name + " [Preprocessed bytecode into " + genericList.size() + " methods.]");
        }
        resetLogger(joinPoint);
        logger.info(name + " [Finished preprocessing bytecode.]");
        return o;
    }

    @Around(value="edu.baylor.ecs.seer.lweaver.aspect.CommonJoinPointConfig.bytecodeServiceProcessBytecode()")
    public Object bytecodeServiceProcessBytecode(ProceedingJoinPoint joinPoint) throws Throwable {
        String name = joinPoint.getSignature().getName();
        resetLogger(joinPoint);
        logger.info(name + " [Processing bytecode into node graphs ... ]");
        Object o = joinPoint.proceed();
        if(o instanceof List<?>){
            List<Map<Integer, FlowNode>> genericList = (List<Map<Integer, FlowNode>>)o;
            resetLogger(joinPoint);
            logger.info(name + " [Processed graphs for " + genericList.size() + " methods.]");
        }
        resetLogger(joinPoint);
        logger.info(name + " [Finished processing methods into node graphs.]");
        return o;
    }

    @Around(value="edu.baylor.ecs.seer.lweaver.aspect.CommonJoinPointConfig.bytecodeServicePostProcessBytecode()")
    public Object bytecodeServicePostProcessBytecode(ProceedingJoinPoint joinPoint) throws Throwable {
        String name = joinPoint.getSignature().getName();
        resetLogger(joinPoint);
        logger.info(name + " [Post processing node graph (" + numProcessed + "/" + numGraphs + ") ... ]");
        Object o = joinPoint.proceed();
        resetLogger(joinPoint);
        logger.info(name + " [Finished post processing node graph (" + numProcessed + "/" + numGraphs + ").]");
        numProcessed++;
        return o;
    }

    @Around(value="edu.baylor.ecs.seer.lweaver.aspect.CommonJoinPointConfig.bytecodeServiceFilter()")
    public Object bytecodeServiceFilter(ProceedingJoinPoint joinPoint) throws Throwable {
        String name = "";
        Object[] args = joinPoint.getArgs();
        if(args.length > 0){
            if(args[0] instanceof CtClass){
                CtClass clazz = (CtClass)args[0];
                name = clazz.getName();
            } else {
                name = "Undetermined";
            }
        }

        resetLogger(joinPoint);
        logger.info("[Filtering class " + name + " ... ]");
        Object o = joinPoint.proceed();
        String result;
        if(o instanceof Boolean){
            Boolean b = (Boolean)o;
            result = b ? "True" : "False";
        } else {
            result = "Undetermined";
        }
        resetLogger(joinPoint);
        logger.info("[Finished filtering class " + name + " with result " + result + ".]");
        return o;
    }


    /*@Around(value="edu.baylor.ecs.seer.lweaver.aspect.CommonJoinPointConfig.bytecodeServiceCall()")
    public Object bytecodeServicePostProcessBytecode(ProceedingJoinPoint joinPoint) throws Throwable {
        String name = joinPoint.getSignature().getName();
        resetLogger(joinPoint);
        logger.info("[Call for " + name + " ... ]");
        Object o = joinPoint.proceed();
        resetLogger(joinPoint);
        logger.info("[Finished " + name + ".]");
        return o;
    }*/

    private void resetLogger(JoinPoint point){
        logger = LoggerFactory.getLogger(point.getSignature().getDeclaringType());
    }


}