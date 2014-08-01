package org.jboss.errai.security.demo.server;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.demo.client.shared.KeycloakActivatorService;

@Service
@ApplicationScoped
public class KeycloakActivatorServiceImpl implements KeycloakActivatorService {

  @Inject
  private ServletContext servletContext;

  @Override
  public boolean isKeycloakActive() {
    return servletContext.getContext("/auth") != null;
  }

}
