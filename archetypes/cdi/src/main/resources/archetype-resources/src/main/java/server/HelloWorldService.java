#set($symbol_pound='#')
    #set($symbol_dollar='$')
    #set($symbol_escape='\' )
/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
    package ${package}.server;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.Service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * A very simple CDI sevice component.
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Jul 21, 2010
 */
@ApplicationScoped
@Service
public class HelloWorldService implements MessageCallback {
  @Inject
  MessageBus bus;

  public void callback(Message message) {
    System.out.println("Received " + message.get(String.class, "payload"));

    MessageBuilder.createConversation(message)
        .subjectProvided()
        .signalling()
        .with("response", "Processed at " + System.currentTimeMillis())
        .done().sendNowWith(bus);
  }
}
