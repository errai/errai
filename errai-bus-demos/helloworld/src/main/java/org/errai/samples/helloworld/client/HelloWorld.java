package org.errai.samples.helloworld.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.errai.bus.client.api.Consumer;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.annotations.ReplyTo;
import org.jboss.errai.bus.client.api.annotations.ToSubject;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@EntryPoint
public class HelloWorld extends VerticalPanel {

    @Inject
    public RequestDispatcher dispatcher;

    @Inject
    @ToSubject("HelloWorld")
    @ReplyTo("ReplyTo")
    public Consumer<String> sender;

    public Label text = new Label();

    @Service("ReplyTo")
    public MessageCallback replyTo = new MessageCallback() {
        public void callback(Message message) {
            text.setText(message.get(String.class, MessageParts.Value));
        }
    };

    @PostConstruct
    public void init() {
        Button button = new Button("hello!");
        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                sender.consume("Hello, World!");
            }
        });

        add(button);
        add(text);

        RootPanel.get().add(this);
    }
}
