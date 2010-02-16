package org.errai.samples.helloworld.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.MessageBus;

import static org.jboss.errai.bus.client.MessageBuilder.createMessage;

public class HelloWorld implements EntryPoint {

  /**
   * Get an instance of the MessageBus
   */
  private MessageBus bus = ErraiBus.get();
  int numMesages = 0;

  public void onModuleLoad() {
    Button clickMe = new Button("Click Me!");

    /**
     * Register a click handler for the button.
     */
    clickMe.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        /**
         * Send a message to the 'HelloWorld' service.
         */
        createMessage()
            .toSubject("HelloWorld")
            .signalling().noErrorHandling()
            .sendNowWith(bus);

        numMesages++;

        History.newItem("msg"+numMesages, false);
      }
    });

    History.addValueChangeHandler(
        new ValueChangeHandler<String>()
        {
          public void onValueChange(ValueChangeEvent<String> event)
          {
            System.out.println("-> "+ event.getValue());
          }
        }
    );
    /**
     * Just add the button to the RootPanel in the DOM.
     */
    RootPanel.get().add(clickMe);
  }
}
