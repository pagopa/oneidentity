package it.pagopa.oneid.common.utils.logging;


import io.quarkus.logging.Log;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Interceptor
@CustomLogging
public class LoggingInterceptor {


  @AroundInvoke
  public Object logMethod(InvocationContext context) throws Exception {

    String patternClassMethod =
        "[" + context.getTarget().getClass().getName() + "." + context.getMethod().getName()
            + "] ";

    Log.debug(patternClassMethod + "start");

    Object result = context.proceed();

    Log.debug(patternClassMethod + "end");

    return result;
  }
}
