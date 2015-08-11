package org.jboss.errai.ioc.client.container;

import com.google.gwt.core.client.js.JsType;

@JsType
public class JsTypeProvider<T> {
  
  // TODO remove 
  public T getInstance(Object ctx) {
    return getBean();
  }
  
  // TODO JsInterop doesn't work when making these methods abstract
  public T getBean() { 
    return null;
  } 
  
  public boolean isSingleton() {
    return false;
  }
}
