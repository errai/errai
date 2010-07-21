/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.workspaces.client.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.json.JSONUtilCli;
import org.jboss.errai.common.client.framework.AcceptsCallback;
import org.jboss.errai.widgets.client.WSModalDialog;
import org.jboss.errai.widgets.client.WSWindowPanel;
import org.jboss.errai.widgets.client.listeners.ClickCallbackListener;
import org.jboss.errai.workspaces.client.protocols.LayoutParts;

import java.util.Map;
import java.util.Set;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createMessage;


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

    public WSTabSelectorDialog(Set<String> components) {
        hPanel = new HorizontalPanel();
        hPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        vPanel = new VerticalPanel();
        hPanel.add(vPanel);
        vPanel.add(message);

        for (String s : components) {

            final Map<String, Object> instanceProperties = JSONUtilCli.decodeMap(s);

            Button b = new Button("<span><img src='" + instanceProperties.get(LayoutParts.IconURI.name())
                    + "' align='left'/>" + instanceProperties.get(LayoutParts.Name.name()) + "</span>"
                    , new ClickHandler() {

                        public void onClick(ClickEvent event) {
                            createMessage()
                                    .toSubject((String) instanceProperties.get(LayoutParts.Subject.name()))
                                    .noErrorHandling().sendNowWith(ErraiBus.get());

                            window.hide();
                        }
                    });

            b.getElement().getStyle().setProperty("background", "transparent");
            b.getElement().getStyle().setProperty("textAlign", "left");
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

        okButton.addClickHandler(okListener);
        buttonPanel.add(okButton);

        cancelButton = new Button("Cancel");

        cancelListener = new ClickCallbackListener(this, AcceptsCallback.MESSAGE_CANCEL);

        cancelButton.addClickHandler(cancelListener);
        buttonPanel.add(cancelButton);

        innerContainer.add(buttonPanel);

        Style s = message.getElement().getStyle();
        s.setProperty("padding", "8px");
        s.setProperty("verticalAlign", "top");

        window.add(hPanel);
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
