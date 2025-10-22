package org.recolnat.collection.manager.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.recolnat.collection.manager.service.AuthenticationService;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class LoggerAspect {
    private final AuthenticationService authenticationService;


    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void resourcePointcut() {
        // Do nothing because this a definition of pointcut.
    }

    @Pointcut("within(recolnat..*.api.web..*)")
    public void resourcePkgPointcut() {
        // Do nothing because this a definition of pointcut.
    }

    @Before("resourcePkgPointcut() && resourcePointcut()")
    public void loggingAdvice(JoinPoint joinPoint){
        log.info("Connected user : {}", authenticationService.findUserAttributes().getUi());

        log.info("Request inputs: {}.{}() with value[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
                Arrays.toString(((CodeSignature)joinPoint.getSignature()).getParameterNames()), Arrays.toString(joinPoint.getArgs()));

    }

    @AfterReturning(pointcut = "resourcePkgPointcut() && resourcePointcut()", returning = "result" )
    public void logAround(Object result){
            log.info("Request result output:  = {}",  result);

    }
}
