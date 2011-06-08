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

package org.jboss.errai.bus.server.security.auth;

import com.google.gwt.core.client.GWT;
import org.jboss.errai.bus.client.api.Message;

/**
 * A simple placeholder adapter which provides no authentication functionality whatsoever.  This can be used for
 * development purposes, if one wishes to avoid authentication for testing.
 */
public class DefaultAdapter implements AuthenticationAdapter {
  public DefaultAdapter() {
    GWT.log("Warning: DefaultAdapter being used. This provides no security.", null);
  }

  public void challenge(Message message) {

  }

  public void process(Message message) {
  }

  public boolean endSession(Message message) {
    return false;
  }

  public boolean isAuthenticated(Message message) {
    return true;
  }

  public boolean requiresAuthorization(Message message) {
    return false;
  }
}
