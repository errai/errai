package org.jboss.errai.js.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import org.jboss.errai.js.client.bus.MsgBus;

/**
 * @author Mike Brock
 */
public class ErraiJS implements EntryPoint {
  @Override
  public void onModuleLoad() {
    GWT.create(MsgBus.class);
    erraiOnLoad();
  }

  private native void erraiOnLoad() /*-{
    if ($wnd.erraiOnLoad && typeof $wnd.erraiOnLoad == 'function') $wnd.erraiOnLoad();
  }-*/;
}
