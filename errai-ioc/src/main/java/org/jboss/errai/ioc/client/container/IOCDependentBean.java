/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.client.container;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;

import javax.enterprise.context.Dependent;

/**
 * Represents a default dependent scoped bean.
 * 
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class IOCDependentBean<T> extends AbstractIOCBean<T> {
  protected final SyncBeanManagerImpl beanManager;
  protected final BeanProvider<T> beanProvider;
  private final Class<Object> beanActivatorType;

  protected IOCDependentBean(final SyncBeanManagerImpl beanManager,
                             final Class<T> type,
                             final Class<?> beanType,
                             final Annotation[] qualifiers,
                             final String name,
                             final boolean concrete,
                             final BeanProvider<T> beanProvider,
                             final Class<Object> beanActivatorType) {
    this.beanManager = beanManager;
    this.type = type;
    this.beanType = beanType;

    if (qualifiers != null) {
      Collections.addAll(this.qualifiers = new HashSet<Annotation>(), qualifiers);
    }
    else {
      this.qualifiers = Collections.emptySet();
    }

    this.name = name;
    this.concrete = concrete;
    this.beanProvider = beanProvider;
    this.beanActivatorType = beanActivatorType;
  }

  public static <T> IOCBeanDef<T> newBean(final SyncBeanManagerImpl beanManager,
                                          final Class<T> type,
                                          final Class<?> beanType,
                                          final Annotation[] qualifiers,
                                          final String name,
                                          final boolean concrete,
                                          final BeanProvider<T> callback,
                                          final Class<Object> beanActivatorType) {
    return new IOCDependentBean<T>(beanManager, type, beanType, qualifiers, name, concrete, callback, beanActivatorType);
  }

  @Override
  public T newInstance() {
    final SimpleCreationalContext context = new SimpleCreationalContext(beanManager, Dependent.class);
    try {
      return beanProvider.getInstance(context);
    }
    finally {
      context.finish();
    }
  }

  @Override
  public T getInstance() {
    final SimpleCreationalContext context = new SimpleCreationalContext(beanManager, Dependent.class);
    try {
      return getInstance(context);
    }
    finally {
      context.finish();
    }
  }

  @Override
  public T getInstance(final CreationalContext context) {
    return beanProvider.getInstance(context);
  }

  @Override
  public Class<? extends Annotation> getScope() {
    return Dependent.class;
  }

  @Override
  public boolean isActivated() {
    if (beanActivatorType == null) { 
      return true;
    }
    
    BeanActivator activator = (BeanActivator) beanManager.lookupBean(beanActivatorType).getInstance();
    return activator.isActivated();
  }
}
