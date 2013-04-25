/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.cdi.demo.mvp.client.local.presenter;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.cdi.demo.mvp.client.local.event.ContactUpdatedEvent;
import org.jboss.errai.cdi.demo.mvp.client.local.event.EditContactCancelledEvent;
import org.jboss.errai.cdi.demo.mvp.client.shared.Contact;
import org.jboss.errai.cdi.demo.mvp.client.shared.ContactsService;
import org.jboss.errai.common.client.api.Caller;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

@Dependent
public class EditContactPresenter implements Presenter {

    public interface Display {
        HasClickHandlers getSaveButton();

        HasClickHandlers getCancelButton();

        HasValue<String> getFirstName();

        HasValue<String> getLastName();

        HasValue<String> getEmailAddress();

        Widget asWidget();
    }

    private Contact contact;

    @Inject
    private Caller<ContactsService> contactsService;

    @Inject
    private HandlerManager eventBus;

    @Inject
    private Display display;

    public EditContactPresenter() {
        this.contact = new Contact();
    }

    private void setContact(String id) {
        contactsService.call(new RemoteCallback<Contact>() {
            public void callback(Contact result) {
                contact = result;
                EditContactPresenter.this.display.getFirstName().setValue(
                    contact.getFirstName());
                EditContactPresenter.this.display.getLastName().setValue(
                    contact.getLastName());
                EditContactPresenter.this.display.getEmailAddress().setValue(
                    contact.getEmailAddress());
            }
        }).getContact(id);
    }

    public void bind() {
        this.display.getSaveButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                doSave();
            }
        });

        this.display.getCancelButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new EditContactCancelledEvent());
            }
        });
    }

    public void go(final HasWidgets container) {
        bind();
        container.clear();
        container.add(display.asWidget());
    }

    public void go(final HasWidgets container, String id) {
        setContact(id);
        go(container);
    }

    private void doSave() {
        contact.setFirstName(display.getFirstName().getValue());
        contact.setLastName(display.getLastName().getValue());
        contact.setEmailAddress(display.getEmailAddress().getValue());

        contactsService.call(new RemoteCallback<Contact>() {
            public void callback(Contact result) {
                eventBus.fireEvent(new ContactUpdatedEvent(result));
            }
        }).updateContact(contact);
    }
}