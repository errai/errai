/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageDeliveryFailure;

import java.lang.reflect.Method;

/**
 * User: christopherbrock
 * Date: 19-Jul-2010
 * Time: 4:57:59 PM
 */
public class MethodEndpointCallback implements MessageCallback {
  private Object instance;
  private Method method;

  public MethodEndpointCallback(Object instance, Method method) {
    this.instance = instance;
    this.method = method;
  }

  public void callback(Message message) {
    try {
      method.invoke(instance, message);
    }
    catch (Exception e) {
      throw new MessageDeliveryFailure(e);
    }
  }
}
