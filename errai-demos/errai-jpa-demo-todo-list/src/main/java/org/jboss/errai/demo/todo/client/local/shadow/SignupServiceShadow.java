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

package org.jboss.errai.demo.todo.client.local.shadow;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.bus.client.api.BusLifecycleAdapter;
import org.jboss.errai.bus.client.api.BusLifecycleEvent;
import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.bus.server.annotations.ShadowService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.demo.todo.shared.RegistrationException;
import org.jboss.errai.demo.todo.shared.SignupService;
import org.jboss.errai.demo.todo.shared.TodoListUser;

/**
 * ShadowService implementation of the SignupService this service will get invoked automatically when the bus
 * is disconnected. It registers a listener when the bus is back online and will then register the user
 * in the background.
 * @author edewit@redhat.com
 */
@ShadowService
public class SignupServiceShadow implements SignupService {
  @Inject
  private EntityManager entityManager;

  @Inject
  private ClientMessageBus bus;

  @Inject
  private Caller<SignupService> signupService;

  @PostConstruct
  private void init() {
    bus.addLifecycleListener(new BusLifecycleAdapter() {
      @Override
      public void busOnline(BusLifecycleEvent event) {
        final List<TempUser> tempUsers = entityManager.createNamedQuery("allTempUsers", TempUser.class).getResultList();
        for (TempUser tempUser : tempUsers) {
          try {
            signupService.call().register(tempUser.asUser(), tempUser.getPassword());
            entityManager.remove(tempUser);
          } catch (RegistrationException e) {
            //TODO maybe here we want to take the user back to the signup page?
            throw new RuntimeException(e);
          }
        }
      }
    });
  }

  @Override
  public TodoListUser register(TodoListUser newUserObject, String password) throws RegistrationException {
    entityManager.persist(new TempUser(newUserObject, password));

    return newUserObject;
  }
}
