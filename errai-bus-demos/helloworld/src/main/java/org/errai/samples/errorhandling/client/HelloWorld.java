package org.errai.samples.errorhandling.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;

public class HelloWorld implements EntryPoint {
    /**
     * Get an instance of the MessageBus
     */
    private MessageBus bus = ErraiBus.get();

    public void onModuleLoad() {
        Button button = new Button("Click Me");
        final Label label = new Label();


        RootPanel.get().add(button);
        RootPanel.get().add(label);
    }
}
