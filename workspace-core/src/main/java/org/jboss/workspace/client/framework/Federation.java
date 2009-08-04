package org.jboss.workspace.client.framework;

import com.google.gwt.user.client.Element;


public class Federation {
    public native static void subscribe(String subject, Element scope, AcceptsCallback callback,
                                        Object subscriberData) /*-{
         $wnd.PageBus.subscribe(subject, scope,
             function(subject, message, subscriberData) {
                callback.@org.jboss.workspace.client.framework.AcceptsCallback::callback(Ljava/lang/Object;)(message)
             }, null);
    }-*/;

    public native static void store(String subject, Object value) /*-{
         $wnd.PageBus.store(subject, value);
    }-*/;

}

