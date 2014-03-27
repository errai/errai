package org.jboss.errai.security.server;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;

public class MessageService extends BaseResponseService {
  
  @RestrictedAccess
  @Service
  public void secureMethod(final Message message) {
    respondToMessage(message);
  }
  
  @Service
  public void insecureMethod(final Message message) {
    respondToMessage(message);
  }
  
  @RestrictedAccess
  @Service
  @Command("command")
  public void secureCommandMethod(final Message message) {
    respondToMessage(message);
  }

}
