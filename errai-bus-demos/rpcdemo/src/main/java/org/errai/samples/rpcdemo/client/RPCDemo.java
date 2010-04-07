package org.errai.samples.rpcdemo.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;

public class RPCDemo implements EntryPoint {
    /**
     * Get an instance of the MessageBus
     */
    private MessageBus bus = ErraiBus.get();

    public void onModuleLoad() {
        final Button checkMemoryButton = new Button("Check Memory Free");
        final Label memoryFreeLabel = new Label();

        final TextBox inputOne = new TextBox();
        final TextBox inputTwo = new TextBox();
        final Button appendTwoStrings = new Button("Append");
        final Label appendResult = new Label();

        checkMemoryButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                MessageBuilder.createCall(new RemoteCallback<Long>() {
                    public void callback(Long response) {
                        memoryFreeLabel.setText("Free Memory: " + response);
                    }
                }, TestService.class).getMemoryFree();
            }
        });                                                     

        appendTwoStrings.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                MessageBuilder.createCall(new RemoteCallback<String>() {
                    public void callback(String response) {
                        appendResult.setText(response);
                    }
                }, TestService.class).append(inputOne.getText(), inputTwo.getText());
            }
        });

      final Button voidReturn = new Button("Test Add" , new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent) {
                MessageBuilder.createCall(new RemoteCallback<Long>() {
                    public void callback(Long response) {
                        appendResult.setText(String.valueOf(response));
                    }
                }, TestService.class).add(Long.parseLong(inputOne.getText()), Long.parseLong(inputTwo.getText()));
            }
      });

        VerticalPanel vPanel = new VerticalPanel();
        HorizontalPanel memoryFreeTest = new HorizontalPanel();
        memoryFreeTest.add(checkMemoryButton);
        memoryFreeTest.add(memoryFreeLabel);
        vPanel.add(memoryFreeTest);

        HorizontalPanel appendTest = new HorizontalPanel();
        appendTest.add(inputOne);
        appendTest.add(inputTwo);
        appendTest.add(appendTwoStrings);
        appendTest.add(appendResult);
        vPanel.add(appendTest);

        vPanel.add(voidReturn);

        RootPanel.get().add(vPanel);
    }
}
