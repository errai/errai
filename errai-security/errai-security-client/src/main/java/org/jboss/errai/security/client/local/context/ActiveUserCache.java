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
package org.jboss.errai.security.client.local.context;

import javax.enterprise.inject.Default;

import org.jboss.errai.security.shared.api.identity.User;

/**
 * Provides a cached copy of the actively logged in user. There are two kinds of
 * implementations for ActiveUserCache:
 * <ol>
 * <li>{@link Simple}
 * <ul>
 * <li>These implementations are purely storage.</li>
 * <li>They <bold>do not</bold> communicate over the network.</li>
 * <li>They <bold>do not</bold> do not update UI elements.</li>
 * <li>They may access browser local storage if
 * {@link SecurityProperties#isLocalStorageOfUserAllowed()} is {@code true}.</li>
 * </ul>
 * </li>
 * <li>{@link Complex}
 * <ul>
 * <li>These <bold>must</bold> perform any actions the {@link Simple} implementation does.</li>
 * <li>They may communicate over the network <bold>on initialization</bold> to
 * populate themselves.</li>
 * <li>They <bold>must</bold> perform any security related activities associated
 * with a change of login status when a new user is set or the cache is
 * invalidated.</li>
 * </ul>
 * </li>
 * </ol>
 * 
 * The {@link Default} implementation is {@link Complex}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface ActiveUserCache {

  /**
   * @return True iff there is a cached {@link User} available from a recent
   *         login.
   */
  public boolean hasUser();

  /**
   * @return The currently logged in {@link User}, or {@literal null}.
   */
  public User getUser();

  /**
   * Manually set the currently logged in {@link User}.
   * 
   * @param user
   *          The {@link User} currently logged in.
   */
  public void setUser(User user);

  /**
   * @return False if the cached {@link User} has been invalidated.
   */
  public boolean isValid();

  /**
   * Invalidate the cached {@link User}.
   */
  public void invalidateCache();

}
