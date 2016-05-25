package org.jboss.errai.ioc.support.bus.tests.client.res;

import javax.inject.Singleton;

@Singleton
public class Greeter {

  public String offline() {
    return "Cucumber water for customers only!";
  }
  
  public String online() {
    return "Enjoy this cup of cucumber water, Jim!";
  }
}
