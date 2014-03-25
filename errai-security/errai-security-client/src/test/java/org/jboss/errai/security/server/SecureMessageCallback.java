package org.jboss.errai.security.server;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;

@RestrictedAccess
@Service
public class SecureMessageCallback extends BaseResponseService implements MessageCallback {

  @Override
  public void callback(final Message message) {
    respondToMessage(message);
  }

}
