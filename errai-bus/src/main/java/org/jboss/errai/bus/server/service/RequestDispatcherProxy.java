package org.jboss.errai.bus.server.service;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.RequestDispatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
public class RequestDispatcherProxy implements RequestDispatcher {
  private List<Message> heldGlobalMessages = new ArrayList<Message>();
  private List<Message> heldMessages = new ArrayList<Message>();
  private RequestDispatcher proxied;
  private boolean proxyClosed;

  @Override
  public void dispatchGlobal(Message message) throws Exception {
    if (proxyClosed) {
      proxied.dispatch(message);
    }
    else {
      heldGlobalMessages.add(message);
    }
  }

  @Override
  public void dispatch(Message message) throws Exception {
    if (proxyClosed) {
      proxied.dispatch(message);
    }
    else {
      heldMessages.add(message);
    }
  }

  void closeProxy(RequestDispatcher dispatcher) {
    try {
      this.proxied = dispatcher;
      this.proxyClosed = true;

      for (Message message : heldMessages) {
        dispatcher.dispatch(message);
      }

      for (Message message : heldGlobalMessages) {
        dispatcher.dispatchGlobal(message);
      }

      heldMessages = null;
      heldGlobalMessages = null;

    }
    catch (Exception e) {
      throw new RuntimeException("failed to close proxy", e);
    }
  }
}
