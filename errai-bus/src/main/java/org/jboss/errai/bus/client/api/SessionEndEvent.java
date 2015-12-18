/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.api;

import org.jboss.errai.common.client.api.Assert;


/**
 * The event object that is delievered to {@link org.jboss.errai.bus.client.api.SessionEndListener}s when a {@link org.jboss.errai.bus.client.api.QueueSession} ends.
 */
public class SessionEndEvent {
  private final QueueSession session;

  /**
   * Creates a SessionEndEvent for the given QueueSession.
   * 
   * @param session the session that is ending. Not null.
   */
  public SessionEndEvent(final QueueSession session) {
    this.session = Assert.notNull(session);
  }

  /**
   * Returns the session that ended.
   * 
   * @return the session that ended. Never null.
   */
  public QueueSession getSession() {
    return session;
  }
}

