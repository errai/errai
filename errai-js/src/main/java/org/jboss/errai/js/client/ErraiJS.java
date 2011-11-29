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
    $wnd.erraiTypeOf = function (value) {
        var s = typeof value;
        if (s === 'object') {
            if (value) {
                if (typeof value.length === 'number' &&
                        !(value.propertyIsEnumerable('length')) &&
                        typeof value.splice === 'function') {
                    s = 'array';
                }
            } else {
                s = 'null';
            }
        }
        return s;
    };

    if ($wnd.erraiOnLoad && typeof $wnd.erraiOnLoad == 'function') $wnd.erraiOnLoad();
  }-*/;
}
