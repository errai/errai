package org.jboss.errai.ioc.client.lifecycle.impl;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ioc.client.lifecycle.api.Destruction;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleEvent;

@Dependent
public class DestructionImpl<T> extends LifecycleEventImpl<T> implements Destruction<T> {

  @Override
  public Class<?> getEventType() {
    return Destruction.class;
  }

}
