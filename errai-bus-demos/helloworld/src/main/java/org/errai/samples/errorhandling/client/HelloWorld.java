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
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;

public class HelloWorld implements EntryPoint {
    /**
     * Get an instance of the RequestDispatcher
     */
    private RequestDispatcher dispatcher = ErraiBus.getDispatcher();

    public void onModuleLoad() {
        Button button = new Button("Click Me", new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                MessageBuilder.createMessage()
                        .toSubject("HelloWorldService")
                        .signalling()
                        .with("msg", "Hi there!")
                        .errorsHandledBy(new ErrorCallback() {
                            public boolean error(Message message, Throwable throwable) {
                                throwable.printStackTrace();
                                return false;
                            }
                        })
                        .sendNowWith(dispatcher);
            }
        });
        final Label label = new Label();

        RootPanel.get().add(button);
        RootPanel.get().add(label);
    }
}
