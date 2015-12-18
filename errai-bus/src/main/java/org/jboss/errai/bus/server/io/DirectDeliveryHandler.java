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

package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.util.BusToolsCli;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.util.LocalContext;
import org.jboss.errai.marshalling.server.util.UnwrappedByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Mike Brock
 */
public class DirectDeliveryHandler implements MessageDeliveryHandler, Wakeable, DirectChannel {
  private static final Logger log = LoggerFactory.getLogger(DirectDeliveryHandler.class);
  private final QueueChannel directSocketChannel;

  public static MessageDeliveryHandler createFor(final QueueChannel channel) {
    return new DirectDeliveryHandler(channel);
  }

  private DirectDeliveryHandler(final QueueChannel directSocketChannel) {
    this.directSocketChannel = directSocketChannel;
  }

  @Override
  public boolean deliver(final MessageQueue queue, final Message message) throws IOException {
    try {
      directSocketChannel.write("[" + BusToolsCli.encodeMessage(message) + "]");
      return true;
    }
    catch (Throwable e) {
      log.info("error writing to socket for queue " + queue.getSession().getSessionId());
      LocalContext.get(queue.getSession()).destroy();
      queue.stopQueue();
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public void onWake(MessageQueue queue) throws IOException {
    UnwrappedByteArrayOutputStream outputStream = new UnwrappedByteArrayOutputStream();
    ByteWriteAdapter adapter = new OutputStreamWriteAdapter(outputStream);
    queue.getBuffer().read(adapter, queue.getBufferColor(), new MultiMessageFilter());

    directSocketChannel.write(new String(outputStream.toByteArray(), 0, outputStream.size()));
  }

  @Override
  public void noop(MessageQueue queue) throws IOException {
  }

  @Override
  public boolean isConnected() {
    return directSocketChannel.isConnected();
  }
}
