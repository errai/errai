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

/**
 * <tt>QueueUnavailableException</tt> extends the <tt>RuntimeException</tt>. It is thrown when a message failed to
 * send or be added to the queue, because the queue is not currently active
 */
public class QueueUnavailableException extends RuntimeException {

  public QueueUnavailableException(String message) {
    super(message);
  }

  public QueueUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
