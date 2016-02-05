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
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.StateSync;
import org.jboss.errai.demo.grocery.client.local.convert.RelativeTimeConverter;
import org.jboss.errai.demo.grocery.client.local.convert.UsernameConverter;
import org.jboss.errai.demo.grocery.client.shared.Item;
import org.jboss.errai.ioc.client.api.LoadAsync;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

@Dependent
@Templated("#main")
@LoadAsync
public class GroceryItemWidget extends Composite implements HasModel<Item> {

  private static Integer NEXT_AVAILABLE_ID = 0;

  private int id;

  @Inject
  protected EntityManager em;

  @Inject
  private @AutoBound
  DataBinder<Item> itemBinder;

  @Inject
  private @Bound
  @DataField
  Label name;

  @Inject
  private @Bound
  @DataField
  Label comment;

  @Inject
  private @Bound(property = "department.name")
  @DataField
  Label department;

  @Inject
  @DataField
  private SimplePanel formHolder;

  @Inject
  private EditForm itemEditForm;

  @Inject
  private Event<ItemEditNotifier> itemEditEvent;

  @Inject
  private Event<ItemEditNotifier> itemDeleteEvent;

  @Inject
  private @Bound
  @DataField
  CheckBox checkBox;

  private @Bound(converter = RelativeTimeConverter.class)
  @DataField
  final Element addedOn = DOM.createSpan();

  private @Bound(converter = UsernameConverter.class)
  @DataField
  final Element addedBy = DOM.createSpan();

  @Inject
  @DataField
  private Button deleteButton;

  @PostConstruct
  public void init() {
    this.id = NEXT_AVAILABLE_ID;
    NEXT_AVAILABLE_ID++;
    deleteButton.addStyleName("hidden");
    formHolder.clear();
    formHolder.add(itemEditForm);
    formHolder.addStyleName("hidden");
  }

  /**
   * Changes the model object visualized by this class to the given one.
   *
   * @param item
   *          The item that should become the model of this class. Must not be null.
   * @return The proxied version of the given item object, for purposes of data binding. If you intend to make any
   *         changes to the state of the item after adding it to this widget, you must do so via this returned proxy. If
   *         you modify the item directly (rather than via the proxy) then this UI widget will not update.
   */
  @Override
  public void setModel(Item item) {
    System.out.println("ItemWidget: adopting model object " + System.identityHashCode(item));
    itemBinder.setModel(item, StateSync.FROM_MODEL);
  }

  @Override
  public Item getModel() {
    return itemBinder.getModel();
  }

  /**
   * The following functions handle user interaction with the web app
   *
   * @param event
   *          The user-generated interactive event
   */
  @EventHandler
  public void onMouseOver(MouseOverEvent event) {
    if (!inEditMode()) {
      String carpeDiem = getModel().getName();
      System.out.println("MouseOver " + carpeDiem);
      addStyleName("active");
      deleteButton.removeStyleName("hidden");
    }
  }

  @EventHandler
  public void onMouseOut(MouseOutEvent event) {
    if (!inEditMode()) {
      System.out.println("MouseOut " + itemBinder.getModel().getName());
      removeStyleName("active");
      deleteButton.addStyleName("hidden");
    }
  }

  @EventHandler
  public void onClick(ClickEvent event) {
    Item item = itemBinder.getModel();
    Boolean currentValue = checkBox.getValue();

    // Tick/untick checkbox
    if (currentValue.equals(false)) {
      checkBox.setValue(true);
      item.setCheckBox(true);
      addStyleName("completed");
    }
    else {
      checkBox.setValue(false);
      item.setCheckBox(false);
      removeStyleName("completed");
    }

    setModel(item);
  }

  @EventHandler
  public void onClick(DoubleClickEvent event) {
    System.out.println("Double click -- edit item");
    itemEditEvent.fire(new ItemEditNotifier(id, ItemEditNotifier.EDIT_EVENT));
  }

  @EventHandler("deleteButton")
  public void onDeleteButtonClicked(ClickEvent event) {
    event.preventDefault();
    System.out.println("Delete item");
    itemEditForm.storeOldItem(getModel(), this.id);
    itemDeleteEvent.fire(new ItemEditNotifier(this.id, ItemEditNotifier.DELETE_EVENT));
  }

  /**
   * Private helper functions
   */
  @SuppressWarnings("unused")
  private void processEditEvent(@Observes ItemEditNotifier notifier) {
    if (notifier.getNotifierType() == ItemEditNotifier.DELETE_EVENT)
      return;

    if (notifier.isCreatedBy(this.id)) {
      if (!inEditMode())
        switchToEditMode();

      Item oldItem = getModel();
      setModel(itemEditForm.getItemBinder().getModel());
      itemEditForm.storeOldItem(oldItem, id);
    }
    else {
      if (inEditMode()) {
        switchToDisplayMode();
      }
    }
  }

  private void switchToEditMode() {
    hideOtherFields();
    formHolder.removeStyleName("hidden");
  }

  private void switchToDisplayMode() {
    formHolder.addStyleName("hidden");
    showOtherFields();

  }

  private boolean inEditMode() {
    if (formHolder.getStyleName().contains("hidden"))
      return false;

    return true;
  }

  private void hideOtherFields() {
    checkBox.addStyleName("hidden");
    comment.addStyleName("hidden");
    department.addStyleName("hidden");
    addedBy.setAttribute("display", "none");
    addedOn.setAttribute("display", "none");
  }

  private void showOtherFields() {
    checkBox.removeStyleName("hidden");
    comment.removeStyleName("hidden");
    department.removeStyleName("hidden");
    addedBy.removeAttribute("display");
    addedOn.removeAttribute("display");
  }

  /**
   * Close edit form from within widget and update widget data
   * @param item
   */
  public void closeEditForm(Item item) {
    setModel(item);
    switchToDisplayMode();
  }

  @PreDestroy
  private void toBeDestroyed() {
    System.out.println("GroceryItemWidget for " + name + " is being destroyed.");
  }
}
