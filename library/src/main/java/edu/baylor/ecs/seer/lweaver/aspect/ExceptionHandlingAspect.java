package edu.baylor.ecs.seer.lweaver.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
@Configurable
public class ExceptionHandlingAspect {

    private Logger logger = LoggerFactory.getLogger(ExceptionHandlingAspect.class);

    @AfterThrowing(value = "edu.baylor.ecs.seer.lweaver.aspect.CommonJoinPointConfig.allMethods()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Exception ex) {
        logger = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringType());
        logger.error("Exception thrown from: " + joinPoint.getSignature().getName());
        logger.error(ex.toString());
    }

}
