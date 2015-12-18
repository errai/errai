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

import java.util.Date;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.validation.ConstraintViolation;

import org.jboss.errai.demo.grocery.client.shared.Department;
import org.jboss.errai.demo.grocery.client.shared.Item;
import org.jboss.errai.ioc.client.api.LoadAsync;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;

/**
 *
 * @author ddadlani
 *
 */

@Dependent
@Templated("ItemForm.html#form")
@LoadAsync
public class EditForm extends Form {

  private Item oldItem;
  private int widgetId;

  @PostConstruct
  private void init() {
    saveButton.setText("Save Changes");
    clearButton.setText("Cancel");
    showOtherFields();
  }

  private void showOtherFields() {
    if ("0px".equals(otherFields.getStyle().getHeight())) {
      new Animation() {
        @Override
        protected void onUpdate(double progress) {
          otherFields.getStyle().setHeight(Window.getClientWidth() > 768 ? 215 : 145 * progress, PX);
        }
      }.run(1000);
    }
  }

  /**
   * Stores old item and fills in form field suggestions with old item data
   *
   * @param item
   *          The old item values
   */
  public void storeOldItem(Item item, int id) {
    this.widgetId = id;
    this.oldItem = item;
    name.setText(oldItem.getName());
    department.setText(oldItem.getDepartment().getName());
    comment.setText(oldItem.getComment());
    setItem(oldItem);
  }

  @Override
  @EventHandler("saveButton")
  public void onSaveButtonClicked(ClickEvent event) {
    if (!isValidName())
      return;
    Department resolvedDepartment = Department.resolve(em, department.getText());

    Item item = retrieveOldItem();
    item.setDepartment(resolvedDepartment);
    item.setAddedBy(user);
    item.setAddedOn(new Date());

    final Set<ConstraintViolation<Item>> violations = validator.validate(item);
    if (violations.size() > 0) {
      ConstraintViolation<Item> violation = violations.iterator().next();
      overallErrorMessage.setText(violation.getPropertyPath() + " " + violation.getMessage());
      overallErrorMessage.setVisible(true);
      return;
    }

    em.merge(groceryList);
    em.flush();

    // hideOtherFields();
    clearButton.click();

    if (afterSaveAction != null) {
      afterSaveAction.run();
    }
  }

  @Override
  @EventHandler("clearButton")
  public void onClearButtonClicked(ClickEvent cancelEvent) {
    GroceryItemWidget parentWidget = (GroceryItemWidget) this.getParent().getParent().getParent();
    parentWidget.closeEditForm(oldItem);
  }

  public void processDeleteEvent(@Observes ItemEditNotifier notifier) {
    if (notifier.isCreatedBy(widgetId)) {

      if (notifier.getNotifierType() == ItemEditNotifier.EDIT_EVENT)
        return;
      Item removeItem = retrieveOldItem();

      em.remove(removeItem);
      groceryList.getItems().remove(removeItem);
      em.flush();
    }
    return;
  }

  private Item retrieveOldItem() {
    int itemIndex = groceryList.getItems().lastIndexOf(oldItem);
    return groceryList.getItems().get(itemIndex);
  }

  @PreDestroy
  private void toBeDestroyed() {
    System.out.println("EditForm is being destroyed.");
  }

}
