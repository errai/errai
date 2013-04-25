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
package org.jboss.errai.demo.grocery.client.local;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.demo.grocery.client.shared.Department;
import org.jboss.errai.demo.grocery.client.shared.GroceryList;
import org.jboss.errai.demo.grocery.client.shared.Item;
import org.jboss.errai.demo.grocery.client.shared.User;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A form for editing the properties of a new or existing Item object.
 * 
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Dependent
@Templated
public class ItemForm extends Composite {

    @Inject private EntityManager em;
    @Inject private User user;
    @Inject private GroceryList groceryList;

    // injecting this data binder causes automatic binding between
    // the properties of Item and the like-named @DataField members in this class
    // Example: property "item.name" tracks the value in the TextBox "name"
    @Inject @AutoBound private DataBinder<Item> itemBinder;

    @Inject @Bound @DataField private SuggestBox name;
    @Inject @Bound @DataField private TextBox comment;
    @Inject @Bound(property="department.name") @DataField private SuggestBox department;
    @Inject @DataField private Button saveButton;

    private Runnable afterSaveAction;
    
    @PostConstruct
    private void setupSuggestions() {
        MultiWordSuggestOracle iso = (MultiWordSuggestOracle) name.getSuggestOracle();
        for (Item i : em.createNamedQuery("allItems", Item.class).getResultList()) {
            iso.add(i.getName());
        }

        MultiWordSuggestOracle dso = (MultiWordSuggestOracle) department.getSuggestOracle();
        for (Department d : em.createNamedQuery("allDepartments", Department.class).getResultList()) {
            dso.add(d.getName());
        }
    }

    @PreDestroy
    void cleanup() {
        itemBinder.unbind();
    }

    @SuppressWarnings("unused")
    private void onNewItem(@Observes Item newItem) {
        System.out.println("ItemForm@" + System.identityHashCode(this) + " got new item event");
        ((MultiWordSuggestOracle) name.getSuggestOracle()).add(newItem.getName());
    }

    /**
     * Returns the store instance that is permanently associated with this form. The returned instance is bound to this store's
     * fields: updates to the form fields will cause matching updates in the returned object's state, and vice-versa.
     * 
     * @return the Item instance that is bound to the fields of this form.
     */
    public Item getItem() {
        return itemBinder.getModel();
    }

    public void setItem(Item item) {
        if (item.getDepartment() == null) {
            item.setDepartment(new Department());
        }
        itemBinder.setModel(item, InitialState.FROM_MODEL);
    }

    /**
     * Gives keyboard focus to the appropriate widget in this form.
     */
    public void grabKeyboardFocus() {
        name.setFocus(true);
    }

    // TODO (after ERRAI-366): make this method package-private
    @EventHandler("saveButton")
    public void onSaveButtonClicked(ClickEvent event) {
        Department resolvedDepartment = Department.resolve(em, department.getText());
        Item item = itemBinder.getModel();
        item.setDepartment(resolvedDepartment);
        item.setAddedBy(user);
        item.setAddedOn(new Date());

        groceryList.getItems().add(item);
        em.persist(groceryList);
        em.flush();

        if (afterSaveAction != null) {
            afterSaveAction.run();
        }
    }

    public void setAfterSaveAction(Runnable afterSaveAction) {
        this.afterSaveAction = afterSaveAction;
    }

}
