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
package org.jboss.errai.security.client.local.api;

import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.errai.ui.nav.client.local.UniquePageRole;

/**
 * Caches information regarding security events and the current user (i.e. which
 * user is logged in, and what was the last page they were rejected from).
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface SecurityContext {

  /**
   * Navigate to the page with a given role, caching the current page so that it
   * may be revisited later.
   * 
   * @see {@link #getLastCachedPageName()}
   * @param roleClass
   *          The role of the page to navigate to.
   * @param lastPage
   *          The name of the last page to cache.
   */
  public void navigateToPage(Class<? extends UniquePageRole> roleClass, String lastPage);

  /**
   * Navigate to the page with a given role.
   * 
   * @param roleClass
   *          The role of the page to navigate to.
   */
  public void navigateToPage(Class<? extends UniquePageRole> roleClass);

  /**
   * Navigate to the last page a user was redirected from (via this security
   * context), or home if the user has not been redirected.
   */
  public void navigateBackOrHome();

  /**
   * @return The name of the last cached page.
   * @see {@link #navigateToPage(Class, String)}
   */
  public String getLastCachedPageName();
  
  /**
   * @return True iff {@link SecurityContext#isCacheValid()} is true and
   *         {@link SecurityContext#getCachedUser()} will not return
   *         {@link User#ANONYMOUS}.
   */
  public boolean hasCachedUser();

  /**
   * Remote calls to {@link AuthenticationService} are cached, such that calls
   * to this method can retrieve the logged in {@link User} from memory.
   * 
   * @return The currently logged in {@link User}. If no user is logged in (or
   *         the cache is {@link SecurityContext#invalidateCache() invalid})
   *         then {@link User#ANONYMOUS}.
   */
  public User getCachedUser();
  
  /**
   * Validate and set the cached user. Subsequent calls to
   * {@link SecurityContext#getCachedUser()} will return the given user until
   * this method or {@link SecurityContext#invalidateCache()} is called.
   * 
   * Subsequent calls to {@link SecurityContext#isCacheValid()} will return true
   * until {@link SecurityContext#invalidateCache()} is called.
   * 
   * @param user
   *          The {@link User} to cache. Must not be {@code null}. To set no
   *          user as logged in, user {@link User#ANONYMOUS}.
   */
  public void setCachedUser(User user);
  
  /**
   * @return True if a call to {@link SecurityContext#getCachedUser()}
   *         accurately reflects the {@link User} that would be returned from a
   *         call to {@link AuthenticationService#getUser()}.
   */
  public boolean isUserCacheValid();
  
  /**
   * Invalidate the cached {@link User}. Subsequent calls to
   * {@link SecurityContext#isUserCacheValid()} will return false.
   */
  public void invalidateCache();
}