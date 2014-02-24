package org.jboss.errai.ui.nav.client.local.lifecycle;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ioc.client.lifecycle.impl.LifecycleEventImpl;

import com.google.gwt.user.client.ui.IsWidget;

@Dependent
public class TransitionEventImpl<W extends IsWidget> extends LifecycleEventImpl<W> implements TransitionEvent<W> {

  @Override
  public Class<?> getEventType() {
    return TransitionEvent.class;
  }

}
