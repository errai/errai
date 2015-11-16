package org.jboss.errai.cdi.interceptors;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class InterceptedBean {

  @SimpleInterceptorBinding
  public void interceptedMethod() {
    System.out.println("yo!");
  }
}
