/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.demo.grocery.client.local;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A container that can hold a widget and show and hide itself on demand. The
 * appearance is based on (and depends on) the Twitter Bootstrap stylesheet
 * collection. It does not require jQuery or the Bootstrap jquery.popup.js
 * plugin (it is a GWT-based replacement for that plugin).
 * <p>
 * Usage Example:
 * <pre>
 * final StoreForm storeForm = beanManager.lookupBean(StoreForm.class).getInstance();
 *   final PopoverContainer popover = beanManager.lookupBean(PopoverContainer.class).getInstance();
 *   popover.setTitleHtml(new SafeHtmlBuilder().appendEscaped("New Store").toSafeHtml());
 *   popover.setBodyWidget(storeForm);
 *   popover.show(addStoreButton);
 *   storeForm.grabKeyboardFocus();
 *
 *   // hide store form when new store is saved
 *   storeForm.setAfterSaveAction(new Runnable() {
 *     {@code @Override}
 *     public void run() {
 *       popover.hide();
 *       beanManager.destroyBean(popover);
 *       beanManager.destroyBean(storeForm);
 *     }
 *   });
 * </pre>
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Templated
@Dependent
public class PopoverContainer extends Composite {

    /**
     * This is the widget that contains the user-supplied title we show in the popover.
     */
    @DataField
    private DivElement popoverTitle = Document.get().createDivElement();

    /**
     * This is the widget that contains the user-supplied content we show in the popover.
     */
    @Inject
    @DataField
    private VerticalPanel popoverContent;

    /**
     * Positions the popover so that its arrow points at the centre of the given widget Makes the popover visible
     *
     * @param positionNear
     */
    public void show(Widget positionNear) {
        getElement().getStyle().setDisplay(Display.BLOCK);
        getElement().getStyle().setLeft(positionNear.getAbsoluteLeft() + positionNear.getOffsetWidth(), Unit.PX);
        getElement().getStyle().setTop(
            positionNear.getAbsoluteTop() + positionNear.getOffsetHeight() / 2
                - getElement().getOffsetHeight() / 2, Unit.PX);
    }

    /**
     * Causes this popover to become invisible.
     */
    public void hide() {
        getElement().getStyle().setDisplay(Display.NONE);
    }

    /**
     * Sets the widget that will be displayed as the title of this popover, replacing any existing title widget.
     */
    public void setTitleHtml(SafeHtml html) {
        popoverTitle.setInnerHTML(html.asString());
    }

    /**
     * Sets the widget that will be displayed as the title of this popover, replacing any existing body widget.
     */
    public void setBodyWidget(Widget bodyWidget) {
        popoverContent.clear();
        popoverContent.add(bodyWidget);
    }

    /**
     * Adds this popover to the document so it can be made visible. This method is called automatically when this bean is
     * created.
     */
    @PostConstruct
    private void init() {
        RootPanel.get().add(this);
    }

    /**
     * Removes this popover from the document so it does not leak resources. This method is called automatically when this bean
     * is destroyed.
     */
    @PreDestroy
    private void destroy() {
        RootPanel.get().remove(this);
    }
}
