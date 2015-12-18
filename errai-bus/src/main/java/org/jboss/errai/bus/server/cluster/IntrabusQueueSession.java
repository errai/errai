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

package org.jboss.errai.bus.server.cluster;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.SessionEndListener;

import java.util.Collection;

/**
 * @author Mike Brock
 */
public class IntrabusQueueSession implements QueueSession {
  public static final QueueSession INSTANCE = new IntrabusQueueSession();

  private IntrabusQueueSession() {
  }

  @Override
  public String getSessionId() {
    return "INTRABUS_SESSION";
  }

  @Override
  public String getParentSessionId() {
    return null;
  }

  @Override
  public boolean endSession() {
    return false;
  }

  @Override
  public void setAttribute(String attribute, Object value) {
  }

  @Override
  public <T> T getAttribute(Class<T> type, String attribute) {
    return null;
  }

  @Override
  public Collection<String> getAttributeNames() {
    return null;
  }

  @Override
  public boolean hasAttribute(String attribute) {
    return false;
  }

  @Override
  public Object removeAttribute(String attribute) {
    return null;
  }

  @Override
  public void addSessionEndListener(SessionEndListener listener) {
  }

  @Override
  public boolean isValid() {
    return true;
  }
}
