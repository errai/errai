package org.jboss.errai.security.client.local.res;

import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;
import org.jboss.errai.security.shared.exception.SecurityException;

import com.google.gwt.http.client.Request;

public class RestErrorCountingCallback implements RestErrorCallback {

  private final Counter counter;
  private final Class<? extends SecurityException> throwType;

  public RestErrorCountingCallback(final Counter counter, final Class<? extends SecurityException> throwType) {
    this.counter = counter;
    this.throwType = throwType;
  }

  @Override
  public boolean error(Request message, Throwable throwable) {
    if (throwable.getClass().equals(throwType)) {
      counter.increment();
      return false;
    }
    else {
      return true;
    }
  }

}
