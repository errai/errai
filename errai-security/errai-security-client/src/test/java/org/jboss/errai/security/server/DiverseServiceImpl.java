package org.jboss.errai.security.server;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.client.shared.DiverseService;

@Service
public class DiverseServiceImpl implements DiverseService {

  @Override
  public void needsAuthentication() {
  }

  @Override
  public void adminOnly() {
  }

  @Override
  public void anybody() {
  }

}
