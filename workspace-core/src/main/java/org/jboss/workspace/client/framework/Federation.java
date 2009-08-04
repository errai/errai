package org.jboss.workspace.client.framework;

import com.google.gwt.core.client.JavaScriptObject;


public class Federation {
    public native static void subscribe(String subject, JavaScriptObject scope, AcceptsCallback callback,
                                        Object subscriberData) /*-{
         $wnd.PageBus.subscribe(subject, scope,
             function(subject, message, subscriberData) { callback.call(message); }, null);
    }-*/;

    public native static void store(String subject, Object value) /*-{
        $wnd.PageBus.store(subject, value);
    }-*/;

}

