package org.jboss.workspace.client.widgets;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.dom.client.Style;
import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.listeners.ClickCallbackListener;
import org.gwt.mosaic.ui.client.WindowPanel;

import java.util.Set;


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

    final WindowPanel window =  new WindowPanel("Select Window");

    String id;

    public WSTabSelectorDialog(Set<WSTab> tabs) {

        hPanel = new HorizontalPanel();

        hPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        vPanel = new VerticalPanel();
        hPanel.add(vPanel);
        vPanel.add(message);

        for (final WSTab tab : tabs)  {

            Button b = new Button(tab.getPacket().getName(), new ClickListener() {
                public void onClick(Widget sender) {
                    tab.activate();
                    window.close();
                }
            });

            if (tab.getPacket().isModifiedFlag()) {
                b.getElement().getStyle().setProperty("color", "blue");
            }

            b.getElement().getStyle().setProperty("background", "transparent");
            b.getElement().getStyle().setProperty("text-align", "left");
            b.setWidth("100%");

            vPanel.add(b);
        }


        HorizontalPanel innerContainer = new HorizontalPanel();
        vPanel.add(innerContainer);
        innerContainer.setWidth("100%");
        innerContainer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

        buttonPanel = new HorizontalPanel();

        okButton = new Button("OK");
        okListener = new ClickCallbackListener(this, AcceptsCallback.MESSAGE_OK);
        okButton.addClickListener(okListener);
        buttonPanel.add(okButton);

        cancelButton = new Button("Cancel");
        cancelListener = new ClickCallbackListener(this, AcceptsCallback.MESSAGE_CANCEL);
        cancelButton.addClickListener(cancelListener);
        buttonPanel.add(cancelButton);

        innerContainer.add(buttonPanel);

        Style s = message.getElement().getStyle();
        s.setProperty("padding", "8px");
        s.setProperty("vertical-align", "top");

        window.add(hPanel);

        window.setAnimationEnabled(true);
        window.setResizable(false);
    }


    public void callback(String message) {
        callbackTo.callback(message);
        window.close();
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
        window.showModal();
    }

}
