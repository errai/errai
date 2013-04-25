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

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.cdi.demo.mvp.client.local.event.AddContactEvent;
import org.jboss.errai.cdi.demo.mvp.client.local.event.EditContactEvent;
import org.jboss.errai.cdi.demo.mvp.client.shared.ContactDetails;
import org.jboss.errai.cdi.demo.mvp.client.shared.ContactsService;
import org.jboss.errai.common.client.api.Caller;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

@Dependent
public class ContactsPresenter implements Presenter {

    private List<ContactDetails> contactDetails;

    public interface Display {
        HasClickHandlers getAddButton();

        HasClickHandlers getDeleteButton();

        HasClickHandlers getList();

        void setData(List<String> data);

        int getClickedRow(ClickEvent event);

        List<Integer> getSelectedRows();

        Widget asWidget();
    }

    @Inject
    private Caller<ContactsService> contactsService;

    @Inject
    private HandlerManager eventBus;

    @Inject
    private Display display;

    public void bind() {
        display.getAddButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new AddContactEvent());
            }
        });

        display.getDeleteButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                deleteSelectedContacts();
            }
        });

        display.getList().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                int selectedRow = display.getClickedRow(event);

                if (selectedRow >= 0) {
                    String id = contactDetails.get(selectedRow).getId();
                    eventBus.fireEvent(new EditContactEvent(id));
                }
            }
        });
    }

    public void go(final HasWidgets container) {
        bind();
        container.clear();
        container.add(display.asWidget());
        fetchContactDetails();
    }

    public void sortContactDetails() {

        // Yes, we could use a more optimized method of sorting, but the
        // point is to create a test case that helps illustrate the higher
        // level concepts used when creating MVP-based applications.
        //
        for (int i = 0; i < contactDetails.size(); ++i) {
            for (int j = 0; j < contactDetails.size() - 1; ++j) {
                if (contactDetails
                    .get(j)
                    .getDisplayName()
                    .compareToIgnoreCase(
                        contactDetails.get(j + 1).getDisplayName()) >= 0) {
                    ContactDetails tmp = contactDetails.get(j);
                    contactDetails.set(j, contactDetails.get(j + 1));
                    contactDetails.set(j + 1, tmp);
                }
            }
        }
    }

    public void setContactDetails(List<ContactDetails> contactDetails) {
        this.contactDetails = contactDetails;
    }

    public ContactDetails getContactDetail(int index) {
        return contactDetails.get(index);
    }

    private void fetchContactDetails() {
        contactsService.call(new RemoteCallback<ArrayList<ContactDetails>>() {
            public void callback(ArrayList<ContactDetails> result) {
                contactDetails = result;
                sortContactDetails();
                List<String> data = new ArrayList<String>();

                for (int i = 0; i < result.size(); ++i) {
                    data.add(contactDetails.get(i).getDisplayName());
                }

                display.setData(data);
            }
        }).getContactDetails();
    }

    private void deleteSelectedContacts() {
        List<Integer> selectedRows = display.getSelectedRows();
        ArrayList<String> ids = new ArrayList<String>();

        for (int i = 0; i < selectedRows.size(); ++i) {
            ids.add(contactDetails.get(selectedRows.get(i)).getId());
        }

        contactsService.call(new RemoteCallback<ArrayList<ContactDetails>>() {
            public void callback(ArrayList<ContactDetails> result) {
                contactDetails = result;
                sortContactDetails();
                List<String> data = new ArrayList<String>();

                for (int i = 0; i < result.size(); ++i) {
                    data.add(contactDetails.get(i).getDisplayName());
                }

                display.setData(data);

            }
        }).deleteContacts(ids);
    }
}
