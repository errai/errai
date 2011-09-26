package org.errai.samples.helloworld.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@EntryPoint
public class HelloWorld extends VerticalPanel {

  private final MessageBus bus;

  @Inject
  public HelloWorld(MessageBus bus) {
    this.bus = bus;
  }

  @PostConstruct
  public void init() {
    Button button = new Button("hello!");

    button.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        MessageBuilder.createMessage()
                .toSubject("HelloWorldService")
                .withValue("Hello, There!")
                .errorsHandledBy(new ErrorCallback() {
                  @Override
                  public boolean error(Message message, Throwable throwable) {
                    throwable.printStackTrace();
                    return true;
                  }
                })
                .repliesTo(new MessageCallback() {
                  public void callback(Message message) {
                    Window.alert("From Server: " + message.get(String.class, MessageParts.Value));
                  }
                })
                .sendNowWith(bus);
      }
    });

    add(button);

    RootPanel.get().add(this);
  }
}
