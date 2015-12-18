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

package org.jboss.errai.security.client.local;

import org.jboss.errai.bus.client.framework.AbstractRpcProxy;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
* @author edewit@redhat.com
*/
@SuppressWarnings("unchecked")
public class SpyAbstractRpcProxy extends AbstractRpcProxy implements AuthenticationService {
  private Multiset<String> calls = HashMultiset.create();

  @Override
  public User login(String username, String password) {
    this.remoteCallback.callback(null);
    calls.add("login");
    return User.ANONYMOUS;
  }

  @Override
  public boolean isLoggedIn() {
    calls.add("isLoggedIn");
    return false;
  }

  @Override
  public void logout() {
    this.remoteCallback.callback(null);
    calls.add("logout");
  }

  @Override
  public User getUser() {
    this.remoteCallback.callback(null);
    calls.add("getUser");
    return User.ANONYMOUS;
  }

  public Integer getCallCount(String method) {
    return calls.count(method);
  }
}
