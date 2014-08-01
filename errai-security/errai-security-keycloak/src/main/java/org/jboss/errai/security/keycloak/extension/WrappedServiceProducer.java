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
package org.jboss.errai.security.keycloak.extension;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.exception.FailedAuthenticationException;
import org.jboss.errai.security.shared.service.AuthenticationService;

/**
 * Produces a dummy {@link AuthenticationService} if no {@link Wrapped} service exists.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@ApplicationScoped
public class WrappedServiceProducer {

  @Inject
  @Wrapped
  private Instance<AuthenticationService> authService;

  private AuthenticationService instance;

  @PostConstruct
  private void init() {
    if (authService.isUnsatisfied()) {
      instance = new DummyAuthenticationService();
    }
    else {
      instance = authService.get();
    }
  }

  @Produces
  @Filtered
  public AuthenticationService getWrappedAuthenticationService() {
    return instance;
  }

  @Alternative
  private static class DummyAuthenticationService implements AuthenticationService {
    @Override
    public User login(String username, String password) {
      throw new FailedAuthenticationException(
              "Must provide a non-keycloak AuthenticationService to use the login method.");
    }

    @Override
    public boolean isLoggedIn() {
      return false;
    }

    @Override
    public void logout() {
    }

    @Override
    public User getUser() {
      return User.ANONYMOUS;
    }
  }
}
