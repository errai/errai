package org.jboss.errai.example.client.local.util;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author edewit@redhat.com
 */
public abstract class DefaultCallback<T> implements AsyncCallback<T> {
  @Override
  public abstract void onSuccess(T result);

  @Override
  public void onFailure(Throwable caught) {
    throw new RuntimeException(caught);
  }
}
