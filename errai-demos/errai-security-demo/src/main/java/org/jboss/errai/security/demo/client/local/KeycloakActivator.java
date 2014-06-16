package org.jboss.errai.security.demo.client.local;

import static com.google.gwt.dom.client.Style.Visibility.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.security.demo.client.shared.KeycloakActivatorService;
import org.jboss.errai.ui.shared.api.style.StyleBindingsRegistry;

import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.Element;

@ApplicationScoped
public class KeycloakActivator {

  private boolean keycloakServerActive;

  @Inject
  private Caller<KeycloakActivatorService> keycloakActivatorService;

  @AfterInitialization
  private void checkKeycloakServer() {
    keycloakActivatorService.call(new RemoteCallback<Boolean>() {
      @Override
      public void callback(final Boolean keycloakServerActive) {
        KeycloakActivator.this.keycloakServerActive = keycloakServerActive;
        StyleBindingsRegistry.get().updateStyles();
      }
    }).isKeycloakActive();
  }

  @Keycloak
  private void hideIfKeycloakDisabled(final Element element) {
    final Visibility visibility = (keycloakServerActive) ? VISIBLE : HIDDEN;
    element.getStyle().setVisibility(visibility);
  }

}
