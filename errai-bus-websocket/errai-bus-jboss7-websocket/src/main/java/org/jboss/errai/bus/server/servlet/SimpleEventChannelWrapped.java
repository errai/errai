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

package org.jboss.errai.bus.server.servlet;

import java.io.IOException;

import org.jboss.as.websockets.WebSocket;
import org.jboss.as.websockets.frame.TextFrame;
import org.jboss.errai.bus.server.io.QueueChannel;

/**
* @author Mike Brock
*/
public class SimpleEventChannelWrapped implements QueueChannel {
  private final WebSocket socket;

  public SimpleEventChannelWrapped(final WebSocket socket) {
    this.socket = socket;
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public void write(final String data) throws IOException {
    socket.writeFrame(TextFrame.from(data));
  }

}
