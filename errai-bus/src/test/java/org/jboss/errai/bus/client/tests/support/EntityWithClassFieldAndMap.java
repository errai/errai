package org.jboss.errai.bus.client.tests.support;

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class EntityWithClassFieldAndMap {

  private Class<?> clazz;
  
  private final Map<String, Object> stuff = new HashMap<>();

  public Class<?> getClazz() {
    return clazz;
  }
  
  public Class<?> getClassFromMap() {
    return (Class<?>) stuff.get(clazz.getName());
  }

  public void setClazz(Class<?> clazz) {
    this.clazz = clazz;
    stuff.put(clazz.getName(), clazz);
  }
  
}
