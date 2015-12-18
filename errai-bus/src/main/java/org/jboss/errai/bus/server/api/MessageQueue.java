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

package org.jboss.errai.bus.server.api;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.server.io.ByteWriteAdapter;
import org.jboss.errai.bus.server.io.MessageDeliveryHandler;
import org.jboss.errai.bus.server.io.buffers.Buffer;
import org.jboss.errai.bus.server.io.buffers.BufferColor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public interface MessageQueue {

  boolean poll(ByteWriteAdapter stream) throws IOException;

  boolean poll(TimeUnit timeUnit, int time, ByteWriteAdapter stream) throws IOException;

  boolean offer(Message message) throws IOException;

  long getCurrentBufferSequenceNumber();

  void wake();

  void setActivationCallback(QueueActivationCallback activationCallback);

  QueueActivationCallback getActivationCallback();

  void fireActivationCallback();

  QueueSession getSession();

  void finishInit();

  boolean isStale();

  boolean isPaged();

  void setPaged(boolean pageStatus);

  boolean isInitialized();

  void heartBeat();

  boolean messagesWaiting();

  void discard();

  void stopQueue();

  Object getActivationLock();

  Object getPageLock();

  MessageDeliveryHandler getDeliveryHandler();

  void setDeliveryHandler(MessageDeliveryHandler handler);

  void setDeliveryHandlerToDefault();

  BufferColor getBufferColor();

  Buffer getBuffer();

  int incrementMessageCount();

  void resetMessageCount();

  long getLastTransmissionTime();

  void setTimeout(long timeout);
}
