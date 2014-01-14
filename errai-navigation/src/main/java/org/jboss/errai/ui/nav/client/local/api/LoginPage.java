package org.jboss.errai.ui.nav.client.local.api;

import org.jboss.errai.ui.nav.client.local.UniquePageRole;

/**
 * Marks this page as the login page so that we can 'redirect' here when authentication of the user is needed.
 *
 * @author edewit@redhat.com
 */
public class LoginPage implements UniquePageRole {
  public static final String CURRENT_PAGE_COOKIE = "currentPageCookie";
}
