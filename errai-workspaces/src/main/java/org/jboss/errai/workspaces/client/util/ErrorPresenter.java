
package org.jboss.errai.workspaces.client.util;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.common.client.framework.AcceptsCallback;
import org.jboss.errai.widgets.client.WSModalDialog;

/**
 * Display error messages
 */
public class ErrorPresenter implements MessageCallback
{
  public void callback(Message message)
  {
    String errorMessage = message.get(String.class, MessageParts.ErrorMessage);

    WSModalDialog errorDialog = new WSModalDialog();
    errorDialog.ask(errorMessage, new AcceptsCallback() {
      public void callback(Object message, Object data) {
      }
    });
    errorDialog.showModal();
  }
}
