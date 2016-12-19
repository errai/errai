/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.client.local.storage;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.marshalling.client.Marshalling;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.api.json.impl.gwt.GWTJSON;
import org.jboss.errai.security.shared.api.identity.User;

import com.google.gwt.json.client.JSONObject;

@IOCProvider
@Singleton
public class StorageHandlerProvider implements Provider<UserStorageHandler> {

  private static class ReadOnlyStorageHandler implements UserStorageHandler {

    ReadOnlyStorageHandler() {
      MarshallerFramework.initializeDefaultSessionProvider();
    }

    @Override
    public User getUser() {
      // If the GWT app's host page is behind a login page, the server
      // can set the currently authenticated user by providing the
      // errai_security_context variable as part of the host page. This way
      // the Errai app can bootstrap and the already authenticated user
      // instance is immediately injectable (without contacting the server
      // first).
      if (ClientSecurityConstants.securityContextObject != null) {
        final EJValue userJson = GWTJSON.wrap(new JSONObject(ClientSecurityConstants.securityContextUserObject));
        return (User) Marshalling.fromJSON(userJson);
      }
      else {
        return null;
      }
    }

    @Override
    public void setUser(final User user) {
    }
  }

  @Override
  public UserStorageHandler get() {
    return new ReadOnlyStorageHandler();
  }

}
