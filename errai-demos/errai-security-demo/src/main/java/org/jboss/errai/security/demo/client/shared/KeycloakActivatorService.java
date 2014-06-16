package org.jboss.errai.security.demo.client.shared;

import org.jboss.errai.bus.server.annotations.Remote;

/**
 * Remote service for detecting if the Keycloak server is active.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Remote
public interface KeycloakActivatorService {

  boolean isKeycloakActive();
}
