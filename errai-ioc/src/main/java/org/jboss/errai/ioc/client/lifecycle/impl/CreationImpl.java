package org.jboss.errai.ioc.client.lifecycle.impl;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ioc.client.lifecycle.api.Creation;

@Dependent
public class CreationImpl<T> extends LifecycleEventImpl<T> implements Creation<T> {

  @Override
  public Class<?> getEventType() {
    return Creation.class;
  }

}
