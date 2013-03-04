package org.jboss.errai.bus.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;

/**
 * @author Mike Brock
 */
@Service
public class ExpiryService implements MessageCallback {
  @Override
  public void callback(final Message message) {
    HttpSession session = message.getResource(HttpServletRequest.class, HttpServletRequest.class.getName()).getSession();
    System.out.println("Expiring session " + session.getId());
    session.invalidate();
  }
}
