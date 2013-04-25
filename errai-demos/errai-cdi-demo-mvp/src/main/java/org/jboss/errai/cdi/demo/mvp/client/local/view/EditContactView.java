package org.jboss.errai.cdi.demo.mvp.client.local.view;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.cdi.demo.mvp.client.local.presenter.EditContactPresenter;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class EditContactView extends Composite implements EditContactPresenter.Display {
  private TextBox firstName = new TextBox();
  private TextBox lastName = new TextBox();
  private TextBox emailAddress = new TextBox();
  
  @Inject UiBinder<Panel, EditContactView> uiBinder;
  
  @UiField DecoratorPanel contentDetailsDecorator;
  @UiField VerticalPanel contentDetailsPanel;
  @UiField FlexTable detailsTable;
  @UiField Button saveButton;
  @UiField Button cancelButton;

  @PostConstruct
  public void init() {
    initWidget(uiBinder.createAndBindUi(this));

    detailsTable.getColumnFormatter().addStyleName(1, "add-contact-input");
    detailsTable.setWidget(0, 0, new Label("Firstname"));
    detailsTable.setWidget(0, 1, firstName);
    detailsTable.setWidget(1, 0, new Label("Lastname"));
    detailsTable.setWidget(1, 1, lastName);
    detailsTable.setWidget(2, 0, new Label("Email Address"));
    detailsTable.setWidget(2, 1, emailAddress);
    firstName.setFocus(true);
  }
  
  public HasValue<String> getFirstName() {
    return firstName;
  }

  public HasValue<String> getLastName() {
    return lastName;
  }

  public HasValue<String> getEmailAddress() {
    return emailAddress;
  }

  public HasClickHandlers getSaveButton() {
    return saveButton;
  }
  
  public HasClickHandlers getCancelButton() {
    return cancelButton;
  }
  
  public Widget asWidget() {
    return this;
  }
}