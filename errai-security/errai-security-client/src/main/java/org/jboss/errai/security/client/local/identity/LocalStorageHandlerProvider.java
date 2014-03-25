/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.errai.security.client.local.identity;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.marshalling.client.Marshalling;
import org.jboss.errai.security.client.local.context.SecurityProperties;
import org.jboss.errai.security.shared.api.identity.User;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.storage.client.Storage;

@IOCProvider
@Singleton
public class LocalStorageHandlerProvider implements Provider<LocalStorageHandler> {

  private class NoopStorageHandler implements LocalStorageHandler {
    @Override
    public User getUser() {
      return null;
    }

    @Override
    public void setUser(final User user) {
    }
  }

  private class LocalStorageHandlerImpl implements LocalStorageHandler {

    private static final String storageKey = "errai-active-user";

    @Override
    public User getUser() {
      final Storage storage = Storage.getLocalStorageIfSupported();
      try {
        final String json = storage.getItem(storageKey);
        if (json != null) {
          return Marshalling.fromJSON(json, User.class);
        }
        else {
          return null;
        }
      }
      catch (RuntimeException e) {
        storage.removeItem(storageKey);
        return null;
      }
    }

    @Override
    public void setUser(final User user) {
      final Storage storage = Storage.getLocalStorageIfSupported();

      if (user != null) {
        final String json = Marshalling.toJSON(user);
        storage.setItem(storageKey, json);
      }
      else {
        storage.removeItem(storageKey);
      }
    }

  }
  
  private final SecurityProperties properties = GWT.create(SecurityProperties.class);

  @Override
  public LocalStorageHandler get() {
    if (Storage.isLocalStorageSupported() && properties.isLocalStorageOfUserAllowed()) {
      return new LocalStorageHandlerImpl();
    }
    else {
      return new NoopStorageHandler();
    }
  }

}
