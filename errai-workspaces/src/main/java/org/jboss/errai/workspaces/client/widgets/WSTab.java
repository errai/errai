/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.workspaces.client.icons.ErraiImageBundle;
import org.jboss.errai.workspaces.client.widgets.dnd.TabDropController;

import java.util.ArrayList;
import java.util.List;


/**
 * A WorkspaceTab is the actual implementation of the rendered tabs along the top of the workspace.
 */
public class WSTab extends Composite {
    WSTabPanel panel;
    Widget widgetRef;

    final Label label;
    TabDropController tabDropController;
    Image icon;
    Image closeButton;

    boolean modifiedFlag = false;

    final HorizontalPanel hPanel = new HorizontalPanel();

    List<CloseHandler<WSTab>> tabCloseHandlers = new ArrayList<CloseHandler<WSTab>>();

    ErraiImageBundle erraiImageBundle = GWT.create(ErraiImageBundle.class);

    //todo: this widget is still tied sendNowWith the Workspace API -- bad!
    public WSTab(String name, Widget widgetRef, Image tabIcon) {
        this.widgetRef = widgetRef;
        this.icon = tabIcon;

        initWidget(hPanel);

        hPanel.add(tabIcon);

        label = new Label(name);
        label.setStylePrimaryName("workspace-TabLabelText");

        hPanel.add(label);

        closeButton = new Image(erraiImageBundle.closeIcon());
        closeButton.addStyleName("workspace-tabCloseButton");
        closeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                notifyCloseHandlers();
            }
        });

        addTabCloseHandler(new CloseHandler<WSTab>() {
            public void onClose(CloseEvent closeEvent) {
                remove();
            }
        });

        hPanel.add(closeButton);

        reset();
    }

    public boolean isModified() {
        return modifiedFlag;
    }

    public void setModified(boolean modified) {
        if (this.modifiedFlag = modified) decorateModified();
    }

    private void decorateModified() {
        label.getElement().getStyle().setProperty("color", "darkblue");
    }

    public Widget getWidgetRef() {
        return widgetRef;
    }

    public void setWidgetRef(Widget widgetRef) {
        this.widgetRef = widgetRef;
    }

    public void reset() {
        sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
    }


    public String toString() {
        return "WSTab:" + label.getText();
    }

    public TabDropController getTabDropController() {
        return tabDropController;
    }

    public Label getLabel() {
        return label;
    }

    public Image getIcon() {
        return icon;
    }

    public boolean isActivated() {
        return panel.getWidgetIndex(widgetRef) == panel.getActiveTab();
    }

    public void activate() {
        panel.selectTab(panel.getWidgetIndex(widgetRef));
    }

    public int remove() {
        int idx = panel.getWidgetIndex(widgetRef);
        panel.remove(idx);
        return idx;
    }

    public void setPanel(WSTabPanel panel) {
        this.panel = panel;
        this.tabDropController = new TabDropController(panel, this);
    }

    public void clearTabCloseHandlers() {
        tabCloseHandlers.clear();
    }

    public void addTabCloseHandler(CloseHandler<WSTab> closeHandler) {
        tabCloseHandlers.add(closeHandler);
    }

    private void notifyCloseHandlers()  {
        WSTabCloseEvent<WSTab> evt = new WSTabCloseEvent<WSTab>(this, false);
        for (CloseHandler<WSTab> handler : tabCloseHandlers) {
            handler.onClose(evt);
        }
    }

    public  class WSTabCloseEvent<T> extends CloseEvent<T> {
        protected WSTabCloseEvent(T target, boolean autoClosed) {
            super(target, autoClosed);
        }
    }

}
