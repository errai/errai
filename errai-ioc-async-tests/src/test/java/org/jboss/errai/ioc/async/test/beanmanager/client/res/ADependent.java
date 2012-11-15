package org.jboss.errai.ioc.async.test.beanmanager.client.res;

import javax.enterprise.context.Dependent;

/**
 * @author Mike Brock
 */
@Dependent
public class ADependent {
  public String testString() {
    return "foo";
  }
}
