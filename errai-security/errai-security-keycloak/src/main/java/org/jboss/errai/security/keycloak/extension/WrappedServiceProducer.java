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
