package org.jboss.errai.security.client.local.nav;

import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListener;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListenerGenerator;

import com.google.gwt.user.client.ui.IsWidget;

public class SingletonGenerator<W extends IsWidget> implements LifecycleListenerGenerator<W> {
  
  private final LifecycleListener<W> listener;
  
  public SingletonGenerator(final LifecycleListener<W> listener) {
    this.listener = listener;
  }

  @Override
  public LifecycleListener<W> newInstance() {
    return listener;
  }
  
  @Override
  public boolean equals(Object obj) {
    return obj != null && obj.getClass().equals(this.getClass());
  }

}
