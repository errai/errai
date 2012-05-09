package org.jboss.errai.cdi.injection.client;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class BeanInjectsNonModuleDependentBean {
  @Inject ArrayList<String> list;

  @PostConstruct
  private void postConstructBean() {
    list.add("foo");
    list.add("bar");
  }

  public ArrayList<String> getList() {
    return list;
  }
}
