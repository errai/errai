package org.jboss.errai.enterprise.client.cdi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.js.JsType;

@JsType
public class WindowEventObservers {
  private Map<String,  List<JsTypeEventObserver<?>>> observers = new HashMap<String, List<JsTypeEventObserver<?>>>();

  public static WindowEventObservers createOrGet() {
    if (!windowEventObserversDefined()) {
      setWindowEventObservers(new WindowEventObservers());
    }
    return getWindowEventObservers();
  }

  public void add(final String eventType, final JsTypeEventObserver<?> observer) {
    if (!observers.containsKey(eventType)) {
      observers.put(eventType, new ArrayList<JsTypeEventObserver<?>>());
    }
    observers.get(eventType).add(observer);
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void fireEvent(final String eventType, final Object evt) {
    for (JsTypeEventObserver observer : get(eventType)) {
      observer.onEvent(evt);
    }
  }
  
  public List<JsTypeEventObserver<?>> get(final String eventType) {
    if (!observers.containsKey(eventType)) {
      return new ArrayList<JsTypeEventObserver<?>>();
    }
    return observers.get(eventType);
  }
  
  private static native WindowEventObservers getWindowEventObservers() /*-{
    return $wnd.eventObservers;
  }-*/;

  private static native void setWindowEventObservers(WindowEventObservers eo) /*-{
    $wnd.eventObservers = eo;
  }-*/;

  private static native boolean windowEventObserversDefined() /*-{
    return !($wnd.eventObservers === undefined);
  }-*/;

}