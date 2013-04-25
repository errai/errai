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
public class
        EditContactPresenter implements Presenter {
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