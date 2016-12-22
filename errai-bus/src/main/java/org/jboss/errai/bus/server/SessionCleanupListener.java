/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Cleans up SessionContainers created by {@link HttpSessionProvider}.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@WebListener
public class SessionCleanupListener implements HttpSessionListener {

  @Override
  public void sessionCreated(final HttpSessionEvent se) {
  }

  @Override
  public void sessionDestroyed(final HttpSessionEvent se) {
    final String id = se.getSession().getId();
    if (HttpSessionProvider.containersByHttpSessionId.remove(id) != null) {
      HttpSessionProvider.log.debug("Removed SessionContainer for session with ID {}", id);
    }
  }

}