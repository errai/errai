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
package org.jboss.errai.tools.source.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.protocols.MessageParts;

class SourceViewWidget extends LayoutPanel {
    private SourcePanel formatted;
    private MessageBus bus = ErraiBus.get();

    public SourceViewWidget(boolean hasInput) {
        super(new BoxLayout(BoxLayout.Orientation.VERTICAL));

        if (hasInput) {
            final TextBox sourceName = new TextBox();

            this.add(sourceName);
            this.add(new Button("View Source", new ClickHandler() {
                public void onClick(ClickEvent clickEvent) {
                    String text = sourceName.getText();
                    requestSource(text);
                }
            }));

        }

        createSourcePanel(this);
        setupListener();
    }

    public SourceViewWidget(String[] sourceNames) {
        super(new BoxLayout(BoxLayout.Orientation.VERTICAL));

        LayoutPanel toolbar = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));
        final ListBox listBox = new ListBox();
        for (String s : sourceNames)
            listBox.addItem(s);
        toolbar.add(listBox, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
        toolbar.add(new Button("View", new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                requestSource(listBox.getItemText(listBox.getSelectedIndex()));
            }
        }));

        this.add(toolbar);

        listBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent changeEvent) {
                requestSource(listBox.getItemText(listBox.getSelectedIndex()));
            }
        });

        createSourcePanel(this);
        setupListener();
    }

    public void requestSource(String className) {
        MessageBuilder.createMessage()
                .toSubject("SourceViewService")
                .with(MessageParts.ReplyTo, "SourceViewClient")
                .with("className", className)
                .noErrorHandling()
                .sendNowWith(bus);
    }

    private void setupListener() {
        bus.subscribe("SourceViewClient", new MessageCallback() {
            public void callback(Message message) {
                String source = message.get(String.class, "source");
                formatted.setSource(source);
            }
        });
    }

    private void createSourcePanel(LayoutPanel panel) {
        formatted = new SourcePanel();
        panel.add(formatted, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));
    }
}