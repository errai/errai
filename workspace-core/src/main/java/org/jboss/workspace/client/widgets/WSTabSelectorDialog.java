package org.jboss.workspace.client.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.layout.WorkspaceLayout;
import org.jboss.workspace.client.listeners.ClickCallbackListener;
import org.jboss.workspace.client.rpc.MessageBusClient;
import org.jboss.workspace.client.rpc.protocols.LayoutCommands;
import org.jboss.workspace.client.rpc.protocols.LayoutParts;

import java.util.HashMap;
import java.util.Map;


public class WSTabSelectorDialog extends WSModalDialog {
    Label message = new Label("Select Window");
    AcceptsCallback callbackTo;

    HorizontalPanel hPanel;
    VerticalPanel vPanel;
    HorizontalPanel buttonPanel;

    Button okButton;
    ClickCallbackListener okListener;

    Button cancelButton;
    ClickCallbackListener cancelListener;

    final WSWindowPanel window = new WSWindowPanel("Select Window");

    String id;

    public WSTabSelectorDialog(String componentTypeId) {
        hPanel = new HorizontalPanel();
        hPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        vPanel = new VerticalPanel();
        hPanel.add(vPanel);
        vPanel.add(message);

        MessageBusClient.subscribe(this.getClass().getName(), new AcceptsCallback() {
            public void callback(Object message, Object data) {
                Map<String, Object> msg = MessageBusClient.decodeMap(message);

                Map<String, Object> nestedData = MessageBusClient.decodeMap(msg.get(LayoutParts.NestedData.name()));
                for (final Map.Entry<String, Object> entry : nestedData.entrySet()) {
                    Map<String, Object> instanceProperties = MessageBusClient.decodeMap(entry.getValue());

                    Button b = new Button("<span><img src='" + instanceProperties.get(LayoutParts.IconURI.name())
                            + "' align='left'/>" + instanceProperties.get(LayoutParts.Name.name()) + "</span>"
                            , new ClickHandler() {

                                public void onClick(ClickEvent event) {
                                    Map<String, Object> msg = new HashMap<String, Object>();
                                    msg.put(LayoutParts.CommandType.name(), LayoutCommands.ActivateTool.name());
                                    MessageBusClient.store(WorkspaceLayout.getInstanceSubject(entry.getKey()), msg);

                                    window.hide();
                                }
                            });

                    b.getElement().getStyle().setProperty("background", "transparent");
                    b.getElement().getStyle().setProperty("textAlign", "left");
                    b.setWidth("100%");

                    vPanel.add(b);
                }


            }
        }, null);


//
//        Map<String, Object> msg = new HashMap<String, Object>();
//        msg.put(CommandProcessor.MessageParts.Subject)
//


//        for (final String tab : tabs) {
//            Button b = new Button("<span><img src='" + tab.getIcon().getUrl() + "' align='left'/>" + tab.getLabel() + "</span>"
//                    , new ClickHandler() {
//
//                        public void onClick(ClickEvent event) {
//                            Map<String, Object> msg = new HashMap<String, Object>();
//
//
//
//                            window.hide();
//                        }
//                    });

//            if (tab.isModified()) {
//                b.getElement().getStyle().setProperty("color", "blue");
//            }

//            b.getElement().getStyle().setProperty("background", "transparent");
//            b.getElement().getStyle().setProperty("textAlign", "left");
//            b.setWidth("100%");
//
//            vPanel.add(b);
//        }

        HorizontalPanel innerContainer = new HorizontalPanel();
        vPanel.add(innerContainer);
        innerContainer.setWidth("100%");
        innerContainer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

        buttonPanel = new

                HorizontalPanel();

        okButton = new

                Button("OK");

        okListener = new

                ClickCallbackListener(this, AcceptsCallback.MESSAGE_OK);

        okButton.addClickHandler(okListener);
        buttonPanel.add(okButton);

        cancelButton = new

                Button("Cancel");

        cancelListener = new

                ClickCallbackListener(this, AcceptsCallback.MESSAGE_CANCEL);

        cancelButton.addClickHandler(cancelListener);
        buttonPanel.add(cancelButton);

        innerContainer.add(buttonPanel);

        Style s = message.getElement().getStyle();
        s.setProperty("padding", "8px");
        s.setProperty("verticalAlign", "top");

        window.add(hPanel);

        System.out.println("Yo!");
    }


    public void callback(Object message, Object data) {
        callbackTo.callback(message, data
        );
        window.hide();
    }

    public void ask(String message, AcceptsCallback callbackTo) {
        this.callbackTo = callbackTo;
        this.message.setText(message);
    }

    public Button getOkButton() {
        return okButton;
    }

    public void setOkButton(Button okButton) {
        this.okButton = okButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public void setCancelButton(Button cancelButton) {
        this.cancelButton = cancelButton;
    }

    public void showModal() {
        window.show();
        window.center();
    }

}
