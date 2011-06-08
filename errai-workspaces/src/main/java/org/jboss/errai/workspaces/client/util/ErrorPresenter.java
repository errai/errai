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

package org.jboss.errai.workspaces.client.util;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.common.client.framework.AcceptsCallback;
import org.jboss.errai.widgets.client.WSModalDialog;

/**
 * Display error messages
 */
public class ErrorPresenter implements MessageCallback {
  public void callback(Message message) {
    String errorMessage = message.get(String.class, MessageParts.ErrorMessage);

    WSModalDialog errorDialog = new WSModalDialog();
    errorDialog.ask(errorMessage, new AcceptsCallback() {
      public void callback(Object message, Object data) {
      }
    });
    errorDialog.showModal();
  }
}
