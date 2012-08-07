package org.jboss.errai.cdi.interceptors;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author Mike Brock
 */
@SimpleInterceptorBinding @Interceptor
public class SimpleInterceptor {

  @AroundInvoke
  public Object intercept(final InvocationContext context) throws Exception {
    System.out.println("Yay!");
    return context.proceed();
  }
}
