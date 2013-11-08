package org.jboss.errai.cdi.producer.server;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.cdi.producer.client.shared.LoggerTestUtil;
import org.jboss.errai.cdi.producer.client.shared.LoggerTestUtil.TestCommand;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.slf4j.Logger;

@Service
public class ClassWithLoggerField implements MessageCallback {

  @Inject private Logger logger;
  @Inject MessageBus bus;
  
  @Override
  public void callback(final Message message) {
    final TestCommand command = TestCommand.valueOf(message.get(String.class, MessageParts.CommandType));
    final boolean res;
    if (command.equals(TestCommand.IS_NOT_NULL)) {
      res = logger != null;
      MessageBuilder.createConversation(message).subjectProvided().with(LoggerTestUtil.RESULT_PART, res).done().sendNowWith(bus);;
    }
    else if (command.equals(TestCommand.IS_CORRECT_NAME)) {
      res = logger.getName().equals(getClass().getName());
      MessageBuilder.createConversation(message).subjectProvided().with(LoggerTestUtil.RESULT_PART, res).done().sendNowWith(bus);
    }
  }
  
}
