package org.jboss.errai.codegen.test.model;

import java.util.Collections;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class BeanWithTypeParmedMeths {
  public static final BeanWithTypeParmedMeths INSTANCE = new BeanWithTypeParmedMeths();

  public Map<Foo, Bar> getFooBarMap() {
    return Collections.emptyMap();
  }

  public void setFooBarMap(Map<Foo, Bar> map) {
    // do nothing;
  }
}
