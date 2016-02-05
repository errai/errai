/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

import static com.google.gwt.dom.client.Style.Unit.PX;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.validation.Validator;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.StateSync;
import org.jboss.errai.demo.grocery.client.shared.Department;
import org.jboss.errai.demo.grocery.client.shared.GroceryList;
import org.jboss.errai.demo.grocery.client.shared.Item;
import org.jboss.errai.demo.grocery.client.shared.User;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;

public abstract class Form extends Composite {

  @Inject
  protected EntityManager em;
  @Inject
  protected Validator validator;
  @Inject
  protected User user;
  @Inject
  protected GroceryList groceryList;
  @Inject
  @AutoBound
  protected DataBinder<Item> itemBinder;
  @Inject
  @DataField
  protected Label overallErrorMessage;
  @Inject
  @Bound
  @DataField
  protected SuggestBox name;
  @Inject
  @Bound
  @DataField
  protected TextBox comment;
  @Inject
  @Bound(property = "department.name")
  @DataField
  protected SuggestBox department;
  @Inject
  @DataField
  protected Button saveButton;
  @Inject
  @DataField
  protected Button clearButton;
  @DataField
  protected Element otherFields = DOM.createDiv();
  protected Runnable afterSaveAction;
  protected HandlerRegistration handlerRegistration;

  public Form() {
    super();
  }

  @PostConstruct
  protected void setupSuggestions() {
    MultiWordSuggestOracle iso = (MultiWordSuggestOracle) name.getSuggestOracle();
    for (Item i : em.createNamedQuery("allItems", Item.class).getResultList()) {
      iso.add(i.getName());
    }
  
    MultiWordSuggestOracle dso = (MultiWordSuggestOracle) department.getSuggestOracle();
    for (Department d : em.createNamedQuery("allDepartments", Department.class).getResultList()) {
      dso.add(d.getName());
    }
  
    name.getValueBox().addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        if ("0px".equals(otherFields.getStyle().getHeight())) {
          new Animation() {
            @Override
            protected void onUpdate(double progress) {
              otherFields.getStyle().setHeight(Window.getClientWidth() > 768 ? 215 : 145 * progress, PX);
            }
          }.run(1000);
        }
      }
    });
    hideOtherFields();
    handlerRegistration = addEnterKeyHandler();
  }

  @PreDestroy
  void cleanup() {
    itemBinder.unbind();
    handlerRegistration.removeHandler();
  }
  
  @EventHandler("saveButton")
  public abstract void onSaveButtonClicked(ClickEvent event);

  
  @EventHandler("clearButton")
  public abstract void onClearButtonClicked(ClickEvent event);
  
  protected void onNewItem(@Observes Item newItem) {
    System.out.println("ItemForm@" + System.identityHashCode(this) + " got new item event");
    ((MultiWordSuggestOracle) name.getSuggestOracle()).add(newItem.getName());
  }

  /**
   * Gives keyboard focus to the appropriate widget in this form.
   */
  protected void grabKeyboardFocus() {
    name.setFocus(true);
  }

  protected void hideOtherFields() {
    otherFields.getStyle().setHeight(0, PX);
  }

  protected HandlerRegistration addEnterKeyHandler() {
    return this.addDomHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
          saveButton.click();
      }
    }, KeyUpEvent.getType());
  }

  protected boolean isValidName() {
    if (name.getText() == null)
      return false;
    else if (name.getText().isEmpty())
      return false;
    else if (name.getText().matches(".*\\w.*"))   //if name.getText() contains at least one word character
      return true;
    
    return false;
  }
  
  public void setItem(Item item) {
    if (item.getDepartment() == null) {
      item.setDepartment(new Department());
    }
    itemBinder.setModel(item, StateSync.FROM_MODEL);
  }
  
  /**
   * Returns the store instance that is permanently associated with this form. The returned instance is bound to this
   * store's fields: updates to the form fields will cause matching updates in the returned object's state, and
   * vice-versa.
   * 
   * @return the Item instance that is bound to the fields of this form.
   */
  public Item getItem() {
    return itemBinder.getModel();
  }

    public DataBinder<Item> getItemBinder() {
    return itemBinder;
  }

}
