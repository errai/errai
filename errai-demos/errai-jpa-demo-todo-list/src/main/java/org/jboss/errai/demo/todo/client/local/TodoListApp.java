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

package org.jboss.errai.demo.todo.client.local;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.jboss.errai.demo.todo.shared.TodoItem;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.ClientBeanManager;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;

@Templated("#main")
@EntryPoint
public class TodoListApp extends Composite {

    @Inject
    EntityManager em;

    @Inject
    ClientBeanManager bm;

    @Inject
    @DataField
    TextBox newItemBox;

    @Inject
    @DataField
    ListWidget<TodoItem, TodoItemWidget> itemContainer;

    @Inject
    @DataField
    Button archiveButton;

    @PostConstruct
    public void init() {
        refreshItems();
    }

    private void refreshItems() {
        TypedQuery<TodoItem> query = em.createNamedQuery("currentItems", TodoItem.class);
        itemContainer.setItems(query.getResultList());
    }

    void onItemChange(@Observes TodoItem item) {
        em.flush();
        refreshItems();
    }

    @EventHandler("newItemBox")
    void onNewItem(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER && !newItemBox.getText().equals("")) {
            TodoItem item = new TodoItem();
            item.setText(newItemBox.getText());
            em.persist(item);
            em.flush();
            newItemBox.setText("");
            refreshItems();
        }
    }

    @EventHandler("archiveButton")
    void archive(ClickEvent event) {
        TypedQuery<TodoItem> query = em.createNamedQuery("currentItems", TodoItem.class);
        for (TodoItem item : query.getResultList()) {
            if (item.isDone()) {
                item.setArchived(true);
            }
        }
        em.flush();
        refreshItems();
    }
}
