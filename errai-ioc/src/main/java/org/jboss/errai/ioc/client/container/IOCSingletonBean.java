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

/**
 * Represents a bean inside the container, capturing the type, qualifiers and instance reference for the bean.
 *
 * @author Mike Brock
 */
public class IOCSingletonBean<T> extends IOCDependentBean<T> {
  private final T instance;

  private IOCSingletonBean(final IOCBeanManager beanManager,
                           final Class<T> type,
                           final Annotation[] qualifiers,
                           final CreationalCallback<T> callback,
                           final T instance) {

    super(beanManager, type, qualifiers, callback);
    this.instance = instance;
  }

  /**
   * Creates a new IOC Bean reference
   *
   * @param type
   *         The type of a bean
   * @param qualifiers
   *         The qualifiers of the bean.
   * @param instance
   *         The instance of the bean.
   * @param <T>
   *         The type of the bean
   *
   * @return A new instance of <tt>IOCSingletonBean</tt>
   */
  public static <T> IOCBeanDef<T> newBean(final IOCBeanManager beanManager,
                                          final Class<T> type,
                                          final Annotation[] qualifiers,
                                          final CreationalCallback<T> callback,
                                          final T instance) {

    return new IOCSingletonBean<T>(beanManager, type, qualifiers, callback, instance);
  }

  @Override
  public T getInstance(final CreationalContext context) {
    return instance;
  }

  public T getInstance() {
    return getInstance(null);
  }
}
