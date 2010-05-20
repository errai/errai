package org.errai.samples.errorhandling.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;

public class HelloWorld implements EntryPoint {
    /**
     * Get an instance of the RequestDispatcher
     */
    private RequestDispatcher dispatcher = ErraiBus.getDispatcher();

    public void onModuleLoad() {
        final Label label = new Label();
        final Button button = new Button("Start");

        ErraiBus.get().subscribe("Timestream", new MessageCallback() {
            public void callback(Message message) {
                label.setText(message.get(String.class, "Data"));
            }
        });

        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                MessageBuilder.createMessage()
                        .toSubject("HelloWorldService")
                        .signalling()
                        .noErrorHandling().sendNowWith(dispatcher);
            }
        });

        RootPanel.get().add(label);
        RootPanel.get().add(button);
    }
}
