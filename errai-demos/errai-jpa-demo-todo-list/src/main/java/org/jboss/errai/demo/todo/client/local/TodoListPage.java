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

package org.jboss.errai.demo.todo.client.local;

import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.demo.todo.shared.SharedList;
import org.jboss.errai.demo.todo.shared.TodoItem;
import org.jboss.errai.demo.todo.shared.TodoListService;
import org.jboss.errai.demo.todo.shared.TodoListUser;
import org.jboss.errai.jpa.sync.client.local.Sync;
import org.jboss.errai.jpa.sync.client.local.SyncParam;
import org.jboss.errai.jpa.sync.client.shared.SyncResponses;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

@RestrictedAccess
@Templated("#main")
@Page(path="list", role = DefaultPage.class)
public class TodoListPage extends Composite {

  @Inject private EntityManager em;
  @Inject private Caller<TodoListService> todoListService;

  private User user; // filled in by @PageShowing method
  @SuppressWarnings("unused")
  private String loginName; // used for jpa-datasync query param

  @Inject private @DataField TextBox newItemBox;
  @Inject private @DataField ListWidget<TodoItem, TodoItemWidget> itemContainer;

  @Inject private @DataField ListWidget<SharedList, SharedListWidget> sharedContainer;

  @Inject private @DataField Button archiveButton;
  @Inject private @DataField Button shareButton;

  @Inject private @DataField Label errorLabel;

  @Inject private @DataField InlineLabel username;

  @Inject private TransitionTo<LoginPage> logoutTransition;
  @Inject private TransitionTo<SharePage> sharePageTransition;
  @Inject private @DataField Anchor logoutLink;

  @Inject private Caller<AuthenticationService> authCaller;

  @PageShowing
  private void onPageShowing() {
    authCaller.call(new RemoteCallback<User>() {

      @Override
      public void callback(final User result) {
        user = result;
        loginName = user.getIdentifier();
        String shortName = user.getProperty(TodoListUser.SHORT_NAME);
        if (shortName == null) {
          shortName = "Anonymous";
        }

        username.setText(shortName);
        errorLabel.setVisible(false);
        refreshItems();

        todoListService.call(new RemoteCallback<List<SharedList>>() {
          @Override
          public void callback(List<SharedList> response) {
            sharedContainer.setItems(response);
          }
        }).getSharedTodoLists();
      }
    }, new BusErrorCallback() {

      @Override
      public boolean error(Message message, Throwable throwable) {
        logoutTransition.go();
        return false;
      }
    }).getUser();
  }

  private void refreshItems() {
    System.out.println("Todo List Demo: refreshItems()");
    TypedQuery<TodoItem> query = em.createNamedQuery("currentItemsForUser", TodoItem.class);
    query.setParameter("userId", user.getIdentifier());
    itemContainer.setItems(query.getResultList());
  }

  void onItemChange(@Observes TodoItem item) {
    em.flush();
    refreshItems();
  }

  @EventHandler("newItemBox")
  void onNewItem(KeyDownEvent event) {
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER && !newItemBox.getText().trim().equals("")) {
      TodoItem item = new TodoItem();
      item.setLoginName(user.getIdentifier());
      item.setText(newItemBox.getText());
      em.persist(item);
      em.flush();
      newItemBox.setText("");
      refreshItems();
    }
  }

  @EventHandler("archiveButton")
  void archive(ClickEvent event) {
    TypedQuery<TodoItem> query = em.createNamedQuery("currentItemsForUser", TodoItem.class);
    query.setParameter("userId", user.getIdentifier());
    for (TodoItem item : query.getResultList()) {
      if (item.isDone()) {
        item.setArchived(true);
      }
    }
    em.flush();
    refreshItems();
  }

  @Sync(query = "allItemsForUser", params = { @SyncParam(name = "userId", val = "{loginName}") })
  void autoSync(final SyncResponses<TodoItem> responses) {
    if (!responses.getResponses().isEmpty()) {
      refreshItems();
    }
  }

  @EventHandler("logoutLink")
  void logout(ClickEvent event) {
    authCaller.call(new RemoteCallback<Void>() {

      @Override
      public void callback(Void response) {
        logoutTransition.go();
      }
    }).logout();
  }

  @EventHandler("shareButton")
  void share(ClickEvent event) {
    sharePageTransition.go();
  }
}
