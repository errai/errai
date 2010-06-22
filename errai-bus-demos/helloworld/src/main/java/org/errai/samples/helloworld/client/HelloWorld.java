package org.errai.samples.helloworld.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.ioc.client.api.ToRootPanel;

@ToRootPanel
public class HelloWorld extends Composite {

    @Inject
    public RequestDispatcher dispatcher;
    
    public HelloWorld() {
        final VerticalPanel panel = new VerticalPanel();

        final Label label = new Label();
        final Button button = new Button("Start/Stop");

        ErraiBus.get().subscribe("DataChannel", new MessageCallback() {
            public void callback(Message message) {
                label.setText(message.get(String.class, "Data"));
            }
        });

        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                MessageBuilder.createMessage()
                        .toSubject("HelloWorldService")
                        .signalling().with(MessageParts.ReplyTo, "DataChannel")
                        .noErrorHandling().sendNowWith(dispatcher);
            }
        });

        panel.add(label);
        panel.add(button);

        initWidget(panel);
    }

}
