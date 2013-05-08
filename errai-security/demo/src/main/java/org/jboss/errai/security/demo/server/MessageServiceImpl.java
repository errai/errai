package org.jboss.errai.security.demo.server;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.demo.client.shared.MessageService;
import org.jboss.errai.security.shared.RequireAuthentication;

/**
 * @author edewit@redhat.com
 */
@Service
public class MessageServiceImpl implements MessageService {
  @Override
  @RequireAuthentication
  public String hello(String name) {
    return "Hello " + name + " how are you";
  }

  @Override
  public String ping() {
    return "pong";
  }
}
