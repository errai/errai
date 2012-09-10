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

import javax.enterprise.context.Dependent;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;

/**
 * Represents a default dependent scoped bean.
 *
 * @author Mike Brock
 */
public class IOCDependentBean<T> extends AbstractIOCBean<T> {
  protected final IOCBeanManager beanManager;
  protected final CreationalCallback<T> creationalCallback;

  protected IOCDependentBean(final IOCBeanManager beanManager,
                             final Class<T> type,
                             final Class<?> beanType,
                             final Annotation[] qualifiers,
                             final String name,
                             final boolean concrete,
                             final CreationalCallback<T> creationalCallback) {
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
    this.creationalCallback = creationalCallback;
  }

  public static <T> IOCBeanDef<T> newBean(final IOCBeanManager beanManager,
                                          final Class<T> type,
                                          final Class<?> beanType,
                                          final Annotation[] qualifiers,
                                          final String name,
                                          final boolean concrete,
                                          final CreationalCallback<T> callback) {
    return new IOCDependentBean<T>(beanManager, type, beanType, qualifiers, name, concrete, callback);
  }

  @Override
  public T newInstance() {
    final CreationalContext context = new CreationalContext(beanManager, Dependent.class.getName());
    try {
      return creationalCallback.getInstance(context);
    }
    finally {
      context.finish();
    }
  }

  @Override
  public T getInstance() {
    final CreationalContext context = new CreationalContext(beanManager, Dependent.class.getName());
    try {
      return getInstance(context);
    }
    finally {
      context.finish();
    }
  }

  @Override
  public T getInstance(final CreationalContext context) {
    return creationalCallback.getInstance(context);
  }

  @Override
  public Class<? extends Annotation> getScope() {
    return Dependent.class;
  }
}
