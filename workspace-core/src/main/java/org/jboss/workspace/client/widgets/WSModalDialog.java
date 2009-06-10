package org.jboss.workspace.client.widgets;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.WindowCloseListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import org.gwt.mosaic.ui.client.WindowPanel;
import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.listeners.ClickCallbackListener;


public class WSModalDialog implements AcceptsCallback {
    Label message = new Label("Warning!");
    AcceptsCallback callbackTo;

    DockPanel dockPanel;

    Button okButton;
    ClickCallbackListener okListener;

    Button cancelButton;
    ClickCallbackListener cancelListener;

    SimplePanel drapePanel = new SimplePanel();
    WSWindowPanel window;

    public WSModalDialog() {
        this("Alert!");
    }

    public WSModalDialog(String title) {
        Style drapeStyle = drapePanel.getElement().getStyle();

        drapeStyle.setProperty("position", "absolute");
        drapeStyle.setProperty("top", "0px");
        drapeStyle.setProperty("left", "0px");

        drapePanel.setWidth("100%");
        drapePanel.setHeight("100%");

        drapePanel.setStyleName("WSWindowPanel-drape");

        window = new WSWindowPanel(title);
        window.setWidth("400px");


//        window.setAnimationEnabled(true);
//        window.setResizable(false);

        dockPanel = new DockPanel();

        dockPanel.add(new Image(GWT.getModuleBaseURL() + "/images/ui/icons/redflag.png"), DockPanel.WEST);
        dockPanel.add(message, DockPanel.CENTER);

        message.getElement().getStyle().setProperty("padding", "5px");

        HorizontalPanel buttonPanel = new HorizontalPanel();

        okButton = new Button("OK");
        okListener = new ClickCallbackListener(this, AcceptsCallback.MESSAGE_OK);
        okButton.addClickHandler(okListener);

        buttonPanel.add(okButton);

        cancelButton = new Button("Cancel");
        cancelListener = new ClickCallbackListener(this, AcceptsCallback.MESSAGE_CANCEL);
        cancelButton.addClickHandler(cancelListener);

        dockPanel.add(cancelButton, DockPanel.SOUTH);

        buttonPanel.add(cancelButton);

        dockPanel.add(buttonPanel, DockPanel.SOUTH);
        dockPanel.setCellHorizontalAlignment(buttonPanel, DockPanel.ALIGN_RIGHT);
        dockPanel.setCellHeight(buttonPanel, "45px");

        window.add(dockPanel);
    }


    public void callback(String message) {
        callbackTo.callback(message);
        window.hide();
    }

    public void ask(String message, final AcceptsCallback callbackTo) {
        this.callbackTo = callbackTo;
        this.message.setText(message);
        window.addClosingHandler(new Window.ClosingHandler() {
            public void onWindowClosing(Window.ClosingEvent event) {
                callbackTo.callback("WindowClosed");
                RootPanel.get().remove(drapePanel);
            }
        });
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

    public void setTitle(String title) {
        this.window.setTitle(title);
    }

    public void showModal() {
        window.center();
        window.show();

     //   drapePanel.getElement().getStyle().setProperty("zIndex", String.valueOf(WSWindowPanel.zIndex - 1));

        RootPanel.get().add(drapePanel);
    }

}
