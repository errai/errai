package org.jboss.errai.bus.client.tests;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.bus.client.api.TransportError;
import org.jboss.errai.bus.client.api.TransportErrorHandler;

class RecordingTransportErrorHandler implements TransportErrorHandler {

  private final List<TransportError> transportErrors = new ArrayList<TransportError>();

  @Override
  public void onError(TransportError error) {
    transportErrors.add(error);
  }

  public List<TransportError> getTransportErrors() {
    return transportErrors;
  }
}
