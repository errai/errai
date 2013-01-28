/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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
import java.util.Collection;

/**
 * @author Mike Brock
 */
public interface SyncBeanManager extends ClientBeanManager {
  void addBean(Class<Object> type,
               Class<?> beanType,
               BeanProvider<Object> callback,
               Object instance,
               Annotation[] qualifiers);

  void addBean(Class<Object> type,
               Class<?> beanType,
               BeanProvider<Object> callback,
               Object instance,
               Annotation[] qualifiers,
               String name);

  void addBean(Class<Object> type,
               Class<?> beanType,
               BeanProvider<Object> callback,
               Object instance,
               Annotation[] qualifiers,
               String name,
               boolean concreteType);

  <T> IOCBeanDef<T> registerBean(IOCBeanDef<T> bean);

  Collection<IOCBeanDef> lookupBeans(String name);

  @SuppressWarnings("unchecked")
  <T> Collection<IOCBeanDef<T>> lookupBeans(Class<T> type);

  @SuppressWarnings("unchecked")
  <T> Collection<IOCBeanDef<T>> lookupBeans(Class<T> type, Annotation... qualifiers);

  @SuppressWarnings("unchecked")
  <T> IOCBeanDef<T> lookupBean(Class<T> type, Annotation... qualifiers);

}
