package org.jboss.errai.cdi.injection.client;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class BeanInjectsNonModuleDependentBeanB {
  @Funject ArrayList<String> funArrayListOfString;

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
