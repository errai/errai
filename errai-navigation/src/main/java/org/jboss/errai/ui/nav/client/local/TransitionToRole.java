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

package org.jboss.errai.ui.nav.client.local;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;

/**
 * This class works like a {@link TransitionTo} but where the target is a {@link UniquePageRole}. By
 * injecting and instance of this class you declare a compile-time dependency on the existence of a
 * {@link Page} with the {@link UniquePageRole} of type {@code U}.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 * @param <U>
 *          The type of {@link UniquePageRole} that this transition navigates to.
 */
public final class TransitionToRole<U extends UniquePageRole> {

  private Class<U> uniquePageRole;

  public TransitionToRole(final Class<U> uniquePageRole) {
    this.uniquePageRole = uniquePageRole;
  }

  public void go() {
    IOC.getAsyncBeanManager().lookupBean(Navigation.class).getInstance(new CreationalCallback<Navigation>() {

      @Override
      public void callback(final Navigation navigation) {
        navigation.goToWithRole(uniquePageRole);
      }
    });
  }

  public Class<U> toUniquePageRole() {
    return uniquePageRole;
  }
}
