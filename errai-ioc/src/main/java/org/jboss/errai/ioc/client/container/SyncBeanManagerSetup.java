/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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
 * @author Mike Brock
 */
public interface SyncBeanManagerSetup {
  /**
   * Register a bean with the manager. This is usually called by the generated code to advertise the
   * bean. Adding beans at runtime will make beans available for lookup through the BeanManager, but
   * will not in any way alter the wiring scenario of auto-discovered beans at runtime.
   * 
   * @param type
   *          the bean type
   * @param beanType
   *          the actual type of the bean
   * @param callback
   *          the creational callback used to construct the bean
   * @param instance
   *          the instance reference
   * @param qualifiers
   *          any qualifiers
   */
  void addBean(Class<Object> type,
               Class<?> beanType,
               BeanProvider<Object> callback,
               Object instance,
               Annotation[] qualifiers);

  /**
   * Register a bean with the manager with a name. This is usually called by the generated code to
   * advertise the bean. Adding beans at runtime will make beans available for lookup through the
   * BeanManager, but will not in any way alter the wiring scenario of auto-discovered beans at
   * runtime.
   * 
   * @param type
   *          the bean type
   * @param beanType
   *          the actual type of the bean
   * @param callback
   *          the creational callback used to construct the bean
   * @param instance
   *          the instance reference
   * @param qualifiers
   *          any qualifiers
   * @param name
   *          the name of the bean
   */
  void addBean(Class<Object> type,
               Class<?> beanType,
               BeanProvider<Object> callback,
               Object instance,
               Annotation[] qualifiers,
               String name);

  /**
   * Register a bean with the manager with a name as well as specifying whether the bean should be
   * treated a concrete type. This is usually called by the generated code to advertise the bean.
   * Adding beans at runtime will make beans available for lookup through the BeanManager, but will
   * not in any way alter the wiring scenario of auto-discovered beans at runtime.
   * 
   * @param type
   *          the bean type
   * @param beanType
   *          the actual type of the bean
   * @param callback
   *          the creational callback used to construct the bean
   * @param instance
   *          the instance reference
   * @param qualifiers
   *          any qualifiers
   * @param name
   *          the name of the bean
   * @param concreteType
   *          true if bean should be treated as concrete (ie. not an interface or abstract type).
   * @param beanActivator
   *          the bean activator type to use.
   */
  void addBean(Class<Object> type,
               Class<?> beanType,
               BeanProvider<Object> callback,
               Object instance,
               Annotation[] qualifiers,
               String name,
               boolean concreteType,
               Class<Object> beanActivatorType);

}
