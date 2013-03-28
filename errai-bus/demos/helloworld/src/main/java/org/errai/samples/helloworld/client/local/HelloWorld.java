package org.errai.samples.helloworld.client.local;

import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.marshalling.client.util.ArraysUtil;

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
    ArraysUtil.testSomeShit(String.class);
//
//    Button button = new Button("hello!");
//
//    button.addClickHandler(new ClickHandler() {
//      public void onClick(ClickEvent event) {
//        MessageBuilder.createMessage()
//                .toSubject("HelloWorldService")
//                .withValue("Hello, There!")
//                .errorsHandledBy(new ErrorCallback<Message>() {
//                  @Override
//                  public boolean error(Message message, Throwable throwable) {
//                    throwable.printStackTrace();
//                    return true;
//                  }
//                })
//                .repliesTo(new MessageCallback() {
//                  public void callback(Message message) {
//                    Window.alert("From Server: " + message.get(String.class, MessageParts.Value));
//                  }
//                })
//                .sendNowWith(bus);
//      }
//    });
//
//    add(button);
//
//    RootPanel.get().add(this);
  }
}
