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

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.demo.todo.shared.ShareList;
import org.jboss.errai.demo.todo.shared.ShareService;
import org.jboss.errai.demo.todo.shared.UnknownUserException;
import org.jboss.errai.demo.todo.shared.TodoListUser;
import org.jboss.errai.security.shared.service.AuthenticationService;

/**
 * @author edewit@redhat.com
 */
@Stateless @Service
public class ShareServiceImpl implements ShareService {

  @Inject AuthenticationService service;
  @Inject EntityManager entityManager;

  @Override
  public void share(String email) throws UnknownUserException {
    org.jboss.errai.security.shared.api.identity.User currentUser = service.getUser();

    //if this was the real world we would sent a mail to the user that this todo list was just shared with him.
    //but this is _only_ a demo.

    final TypedQuery<TodoListUser> query = entityManager.createNamedQuery("userByEmail", TodoListUser.class);
    query.setParameter("email", email);
    TodoListUser user;
    try {
      user = query.getSingleResult();
    } catch (NoResultException exception) {
      throw new UnknownUserException("user with email '" + email + "' is not a registered user");
    }

    ShareList shareList;
    try {
      shareList = entityManager.createNamedQuery("mySharedLists", ShareList.class)
              .setParameter("loginName", currentUser.getIdentifier()).getSingleResult();
    } catch (NoResultException e) {
      shareList = new ShareList();
      shareList.setUser(entityManager.find(TodoListUser.class, currentUser.getIdentifier()));
    }

    shareList.getSharedWith().add(user);

    entityManager.persist(shareList);
    entityManager.flush();
  }
}
