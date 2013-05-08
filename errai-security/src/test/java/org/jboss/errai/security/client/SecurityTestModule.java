package org.jboss.errai.security.client;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.security.client.local.Identity;

import javax.inject.Inject;

/**
 * @author edewit@redhat.com
 */
@EntryPoint
public class SecurityTestModule {

  @Inject
  Identity identity;

  void login() {
    identity.login();
  }
}
