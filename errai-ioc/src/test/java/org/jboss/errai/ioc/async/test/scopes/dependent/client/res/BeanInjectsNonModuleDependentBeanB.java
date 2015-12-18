package org.jboss.errai.ioc.async.test.scopes.dependent.client.res;

import org.jboss.errai.ioc.client.api.LoadAsync;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;

/**
 * @author Mike Brock
 */
@Singleton @LoadAsync
public class BeanInjectsNonModuleDependentBeanB {
  @Inject ArrayList<String> funArrayListOfString;

  @PostConstruct
  private void postConstructBean() {
    funArrayListOfString.add("foo");
    funArrayListOfString.add("bar");
  }

  public ArrayList<String> getFunArrayListOfString() {
    return funArrayListOfString;
  }

  public void setFunArrayListOfString(ArrayList<String> funArrayListOfString) {
    this.funArrayListOfString = funArrayListOfString;
  }
}
