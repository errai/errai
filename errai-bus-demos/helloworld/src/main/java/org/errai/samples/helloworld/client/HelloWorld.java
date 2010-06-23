package org.errai.samples.helloworld.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.ioc.client.api.ToRootPanel;

import javax.annotation.PostConstruct;

@ToRootPanel
public class HelloWorld extends Composite {
    @Inject
    public RequestDispatcher dispatcher;

    @Inject
    public MessageBus bus;

    final Button sayHello;
    final Label data;

    public HelloWorld() {
        sayHello = new Button("Say Hello!");
        data = new Label();

        VerticalPanel panel = new VerticalPanel();
        panel.add(sayHello);
        panel.add(data);

        initWidget(panel);
    }

    @PostConstruct
    public void init() {
        bus.subscribe("DataConsumer", new MessageCallback() {
            public void callback(Message message) {
                data.setText(message.get(String.class, "Data"));
            }
        });

        sayHello.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                MessageBuilder.createMessage()
                        .toSubject("HelloWorldService")
                        .signalling()
                        .with(MessageParts.ReplyTo, "DataConsumer")
                        .noErrorHandling().sendNowWith(dispatcher);
            }
        });
    }
}
