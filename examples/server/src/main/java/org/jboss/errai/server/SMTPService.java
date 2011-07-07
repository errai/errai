/* jboss.org */
package org.jboss.errai.server;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 22, 2010
 */
@ApplicationScoped
@Service
public class SMTPService implements MessageCallback {

  private static final Logger log =
      LoggerFactory.getLogger(SMTPService.class);

  @Inject
  MessageBus bus;

  // Handle errors locally. Might be the case that no listener is registered
  private final ErrorCallback errorhandler = new ErrorCallback()
  {
    public boolean error(Message message, Throwable t)
    {
      log.error("SMTP service execution error", t);
      return false;
    }
  };

  public void callback(Message message) {
    log.info("Processing: " + message.get(String.class, "body"));
  }
}
