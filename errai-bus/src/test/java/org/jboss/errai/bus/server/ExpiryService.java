package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Mike Brock
 */
@Service
public class ExpiryService implements MessageCallback {
  @Override
  public void callback(final Message message) {
    message.getResource(HttpServletRequest.class, HttpServletRequest.class.getName()).getSession().invalidate();
  }
}
