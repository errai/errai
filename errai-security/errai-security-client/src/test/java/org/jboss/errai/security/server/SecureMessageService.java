package org.jboss.errai.security.server;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;

@RestrictedAccess
public class SecureMessageService extends BaseResponseService {
  
  @Service
  public void methodInSecureClass(final Message message) {
    respondToMessage(message);
  }
  
  @Service
  @Command("command")
  public void commandMethodInSecureClass(final Message message) {
    respondToMessage(message);
  }

}
