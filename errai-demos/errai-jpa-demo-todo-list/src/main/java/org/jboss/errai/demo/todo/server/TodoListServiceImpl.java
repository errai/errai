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

package org.jboss.errai.demo.todo.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.demo.todo.shared.SharedList;
import org.jboss.errai.demo.todo.shared.TodoItem;
import org.jboss.errai.demo.todo.shared.TodoListService;
import org.jboss.errai.demo.todo.shared.TodoListUser;
import org.jboss.errai.security.shared.service.AuthenticationService;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * @author edewit@redhat.com
 */
@Stateless
@Service
public class TodoListServiceImpl implements TodoListService {
  @Inject
  private EntityManager entityManager;
  @Inject
  private AuthenticationService service;

  @Override
  public List<SharedList> getSharedTodoLists() {
    final ArrayList<SharedList> sharedLists = new ArrayList<SharedList>();
    org.jboss.errai.security.shared.api.identity.User currentUser = service.getUser();
    final TypedQuery<TodoListUser> query = entityManager.createNamedQuery("sharedWithMe", TodoListUser.class);
    query.setParameter("loginName", currentUser.getIdentifier());

    final Map<String, TodoListUser> userNames = Maps.uniqueIndex(query.getResultList(), new Function<TodoListUser, String>() {
      @Override
      public String apply(TodoListUser input) {
        return input != null ? input.getLoginName() : "";
      }
    });

    final Set<String> sharedWithMe = userNames.keySet();

    if (!sharedWithMe.isEmpty()) {
      final List<TodoItem> list = entityManager.createNamedQuery("allSharedItems", TodoItem.class)
              .setParameter("userIds", sharedWithMe).getResultList();

      String userName = null;
      SharedList currentList = null;
      for (TodoItem todoItem : list) {
        if (!todoItem.getLoginName().equals(userName)) {
          currentList = new SharedList(userNames.get(todoItem.getLoginName()).getShortName());
          sharedLists.add(currentList);
          userName = todoItem.getLoginName();
        }

        currentList.getItems().add(todoItem);
      }
    }
    return sharedLists;
  }
}
