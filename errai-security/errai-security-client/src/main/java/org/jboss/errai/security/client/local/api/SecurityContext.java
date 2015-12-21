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

package org.jboss.errai.security.client.local.api;

import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;
import org.jboss.errai.ui.nav.client.local.api.SecurityError;

/**
 * Caches information regarding security events and the current user (i.e. which
 * user is logged in, and what was the last page they were rejected from).
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface SecurityContext {

  /**
   * Navigate to the {@link LoginPage}, caching the current page. This is the same as calling
   * {@link #redirectToLoginPage(Class)} with the argument {@link Navigation#getCurrentPage()
   * Navigation.getCurrentPage().contentType()}.
   */
  public void redirectToLoginPage();

  /**
   * Navigate to the {@link LoginPage}.
   *
   * @param fromPage
   *          This {@link Page} type is cached so that a subsequent call to
   *          {@link #navigateBackOrHome()} or {@link #navigateBackOrToPage(Class)} will return to
   *          the {@link Page} of this type.
   */
  public void redirectToLoginPage(Class<?> fromPage);

  /**
   * Navigate to the {@link SecurityError}, caching the current page. This is the same as calling
   * {@link #redirectToSecurityErrorPage(Class)} with the argument
   * {@link Navigation#getCurrentPage() Navigation.getCurrentPage().contentType()}.
   */
  public void redirectToSecurityErrorPage();

  /**
   * Navigate to the {@link SecurityError} page.
   *
   * @param fromPage
   *          This {@link Page} type is cached so that a subsequent call to
   *          {@link #navigateBackOrHome()} or {@link #navigateBackOrToPage(Class)} will return to
   *          the {@link Page} of this type.
   */
  public void redirectToSecurityErrorPage(Class<?> fromPage);

  /**
   * Navigate to the last page a user was redirected from (via this security context), or to the
   * {@link DefaultPage} if the user has not been redirected.
   */
  public void navigateBackOrHome();

  /**
   * Navigate to the last page a user was redirected from (via this security context), or to the
   * given page if the user has not been redirected.
   *
   * @param pageType
   *          The type of the page to navigate to if the user has not been redirected. Must not be
   *          {@code null}.
   */
  public void navigateBackOrToPage(Class<?> pageType);

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
