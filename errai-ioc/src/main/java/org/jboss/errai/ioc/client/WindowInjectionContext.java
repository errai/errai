package org.jboss.errai.ioc.client;

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.ioc.client.container.IOCResolutionException;

import com.google.gwt.core.client.js.JsType;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Max Barkley <mbarkley@redhat.com>
 */
@JsType
public class WindowInjectionContext {
  private Map<String, Object> singletonBeans = new HashMap<String, Object>(); 

  public static WindowInjectionContext createOrGet() {
    if (!isWindowInjectionContextDefined()) {
      setWindowInjectionContext(new WindowInjectionContext());
    }
    return getWindowInjectionContext();
  }

  public static native WindowInjectionContext getWindowInjectionContext() /*-{
    return $wnd.injectionContext;
  }-*/;

  public static native void setWindowInjectionContext(WindowInjectionContext ic) /*-{
    $wnd.injectionContext = ic;
  }-*/;

  public static native boolean isWindowInjectionContextDefined() /*-{
    return !($wnd.injectionContext === undefined);
  }-*/;
  
  public void addSingletonBean(final String name, final Object instance) {
    singletonBeans.put(name, instance);
  }
  
  public void addSuperTypeAlias(final String superTypeName, final String typeName) {
    final Object bean = singletonBeans.get(typeName);
    
    if (bean != null) {
      singletonBeans.put(superTypeName, bean);
    }
    
    // TODO dependent scope
  }
  
  public Object getBean(final String name) {
    final Object bean = singletonBeans.get(name);
    
    // TODO dependent scope
    
    if (bean == null) {
      throw new IOCResolutionException("no matching bean instances for: " + name);
    }
    return bean;
  }

}