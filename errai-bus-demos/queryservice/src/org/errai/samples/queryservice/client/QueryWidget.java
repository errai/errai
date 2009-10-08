package org.errai.samples.queryservice.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.ErraiClient;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;

public class QueryWidget extends Composite {
    /**
     * Do boilerplate for UIBinder
     */
    @UiTemplate("QueryWidget.ui.xml")
    interface Binder extends UiBinder<Panel, QueryWidget> {
    }

    private static final Binder binder = GWT.create(Binder.class);

    {
        initWidget(binder.createAndBindUi(this));
    }

    @UiField
    TextBox queryBox;

    @UiField
    HTML results;

    private MessageBus bus = ErraiClient.getBus();

    @UiHandler("sendQuery")
    void doSubmit(ClickEvent event) {
        CommandMessage msg = CommandMessage.create()
                .toSubject("QueryService")
                .set("QueryString", queryBox.getText());

        MessageCallback responseHandler = new MessageCallback() {
            public void callback(CommandMessage message) {

                String[] resultsString = message.get(String[].class, "QueryResponse");

                if (resultsString == null) {
                    resultsString = new String[] { "No results." };
                }

                StringBuffer buf = new StringBuffer("<ul>");

                for (String result : resultsString) {
                    buf.append("<li>").append(result).append("</li>");
                }

                results.setHTML(buf.append("</ul>").toString());
            }
        };

        bus.conversationWith(msg, responseHandler);
    }

}



