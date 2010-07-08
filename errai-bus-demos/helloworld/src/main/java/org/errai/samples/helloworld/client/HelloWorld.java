package org.errai.samples.helloworld.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.ioc.client.api.InjectPanel;
import org.jboss.errai.ioc.client.api.ToPanel;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

@ToPanel("MyPanel")
@Service("DataConsumer")
@Singleton
public class HelloWorld extends SimplePanel implements MessageCallback {
    @Inject
    private RequestDispatcher dispatcher;

    @Inject
    private MyPanel panel;

    final Button sayHello;
    final Label data;

    public void callback(Message message) {
        data.setText(message.get(String.class, "Data"));
    }

    public HelloWorld() {
        sayHello = new Button("Say Hello!");
        data = new Label();
    }

    @PostConstruct
    public void init() {
        panel.add(sayHello);
        panel.add(data);

        add(panel);

        sayHello.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                MessageBuilder.createMessage()
                        .toSubject("HelloWorldService")
                        .with(MessageParts.ReplyTo, "DataConsumer")
                        .noErrorHandling().sendNowWith(dispatcher);
            }
        });
    }

    public void setDispatcher(RequestDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void setPanel(MyPanel panel) {
        this.panel = panel;
    }
}
