/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
