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

package org.jboss.errai.security.client.local.spi;

import org.jboss.errai.security.shared.api.identity.User;

/**
 * Provides a cached copy of the actively logged in user.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface ActiveUserCache {

  /**
   * When this returns {@code false}, calls to {@link ActiveUserCache#getUser()}
   * will return {@link User#ANONYMOUS}.
   * 
   * @return True iff there is a cached {@link User} available from a recent
   *         login.
   */
  public boolean hasUser();

  /**
   * @return The currently logged in {@link User}. Never returns {@code null}.
   *         If no user is logged in, returns {@link User#ANONYMOUS}.
   */
  public User getUser();

  /**
   * Set the currently logged in {@link User}.
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
