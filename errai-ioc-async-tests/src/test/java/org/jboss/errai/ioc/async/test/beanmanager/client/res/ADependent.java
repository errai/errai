package org.jboss.errai.ioc.async.test.beanmanager.client.res;

import org.jboss.errai.ioc.client.api.LoadAsync;

import javax.enterprise.context.Dependent;

/**
 * @author Mike Brock
 */
@Dependent @LoadAsync
public class ADependent {
  public String testString() {
    return "foo";
  }
}
