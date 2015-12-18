package org.jboss.errai.ioc.support.bus.tests.server;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.common.client.protocols.MessageParts;

import java.util.ArrayList;
import java.util.List;

/**
 * A service that accepts lists of strings, converts all strings in uppercase,
 * then replies back with the list of capitalized strings.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Service
public class ListCapitializationService implements MessageCallback {

  public static List<String> capitalize(List<String> incomingList) {
    List<String> capitalized = new ArrayList<String>(incomingList.size());
    for (String s : incomingList) {
      capitalized.add(s == null ? null : s.toUpperCase());
    }
    return capitalized;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void callback(Message message) {
    MessageBuilder.createConversation(message)
    .subjectProvided()
    .withValue(capitalize(message.get(List.class, MessageParts.Value)))
    .done().reply();
  }
}
