package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.annotations.security.RequireAuthentication;
import org.jboss.errai.bus.server.annotations.security.RequireRoles;

/**
 * @author Mike Brock
 */
@Service("TestSvcAuth")
@RequireAuthentication
public class ServiceWithMultipleEndpointsWithAuthentication {

  @Command("foo")
  public void foo(Message message) {
    MessageBuilder.createConversation(message)
            .subjectProvided()
            .with("Msg", "Foo!")
            .done().reply();
  }

  @Command("bar")
  public void bar(Message message) {
    MessageBuilder.createConversation(message)
            .subjectProvided()
            .with("Msg", "Bar!")
            .done().reply();
  }

}
