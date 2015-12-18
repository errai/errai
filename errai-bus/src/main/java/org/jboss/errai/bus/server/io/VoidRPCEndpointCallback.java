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

package org.jboss.errai.bus.server.io;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createConversation;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;

import java.lang.reflect.Method;

/**
 * @author Mike Brock
 */
public class VoidRPCEndpointCallback extends AbstractRPCMethodCallback {
  public VoidRPCEndpointCallback(ServiceInstanceProvider genericSvc, Method method, MessageBus bus) {
    super(genericSvc, method, bus);
  }

  @Override
  public void callback(final Message message) {
    invokeMethodFromMessage(message);
    createConversation(message)
        .subjectProvided()
        .noErrorHandling().sendNowWith(bus);
  }
}
