package org.jboss.errai.security;

import org.jboss.errai.security.shared.RequireAuthentication;

/**
 * @author edewit@redhat.com
 */
public class TestService {

  @RequireAuthentication
  public void testMethod() {

  }
}
