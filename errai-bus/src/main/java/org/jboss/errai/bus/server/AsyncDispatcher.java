/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.bus.server.service.ErraiService;

/**
 * The <tt>AsyncDispatcher</tt> provides asynchronous message delivery into the bus.  This means that incoming remote
 * requests do not block, and processing of the request continues even after the incoming network conversation has
 * ended.
 * </p>
 * This dispatcher implementation can be used with the {@link org.jboss.errai.bus.server.servlet.DefaultBlockingServlet}
 * as this pertains to incoming--as opposed to outgoing--message handling. Note: some appservers or servlet environments
 * may restrict thread creation within the container, in which case this implementation cannot be used.
 *
 * @author Mike Brock
 */
@Singleton
public class AsyncDispatcher implements RequestDispatcher {
  private WorkerFactory workerFactory;
  private ErraiService service;

  /**
   * Constructs the <tt>AsyncDispatcher</tt> with the specified service. The injection makes it possible to obtain
   * a reference to the <tt>ErraiService</tt>
   *
   * @param service the service where the bus is located
   */
  @Inject
  public AsyncDispatcher(final ErraiService service) {
    this.service = service;
    this.workerFactory = new WorkerFactory(service);

    service.addShutdownHook(new Runnable() {
      @Override
      public void run() {
        workerFactory.stopPool();
      }
    });
  }

  /**
   * Sends the message globally. If the <tt>PriorityProcessing</tt> routing flag is set, then the message is sent
   * globally on the bus. If not, the message is sent globally through the <tt>workerFactory</tt>
   *
   * @param message a message to dispatch globally
   */
  public void dispatchGlobal(final Message message) throws InterruptedException {
    if (message.hasPart(MessageParts.PriorityProcessing)) {
      try {
        service.getBus().sendGlobal(message);
      }
      catch (Throwable t) {
        if (message.getErrorCallback() != null) {
          if (!message.getErrorCallback().error(message, t)) {
            return;
          }
        }
        else {
          t.printStackTrace();
        }
      }
    }
    else {
      workerFactory.deliverGlobal(message);
    }
  }

  /**
   * @param message a message to dispatch
   */
  public void dispatch(Message message) throws InterruptedException {
    workerFactory.deliver(message);
  }
}
