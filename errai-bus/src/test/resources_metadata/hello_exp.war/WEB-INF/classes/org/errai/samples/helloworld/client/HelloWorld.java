package org.errai.samples.helloworld.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.util.SimpleMessage;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@EntryPoint
@Service
public class HelloWorld extends VerticalPanel implements MessageCallback {
  private Button sayHello;
  private Label label;

  private MessageBus bus;

  @Inject
  public HelloWorld(MessageBus bus) {
    this.bus = bus;
  }

  public void callback(Message message) {
    label.setText(SimpleMessage.get(message));
  }

  @PostConstruct
  public void init() {
    sayHello = new Button("Say Hello!");

    /**
     * Register click handler.
     */
    sayHello.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        MessageBuilder.createMessage()
                .toSubject("HelloWorldService")
                .with(MessageParts.ReplyTo, "HelloWorld")
                .with("TestLong", 1000l)
                .with("TestDouble", 1500.55d)
                .done().sendNowWith(bus);
      }
    });

    label = new Label();

    add(sayHello);
    add(label);

    RootPanel.get().add(this);
  }
}
