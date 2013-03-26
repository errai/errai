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

/**
 * A bean provider is used for defining the actual procedure for generating a bean. It is responsible for
 * instantiating and wiring the bean. This interface, however, is implemented by the Errai code generators directly
 * and is not meant to be used directly by users.
 *
 * @author Mike Brock
 */
public interface BeanProvider<T> {

  /**
   * Returns a new instance of the bean which this <tt>BeanProvider</tt> represents. It does not however, return
   * the bean in a ready-to-use state. Any {@link InitializationCallback} tasks or {@link ProxyResolver} tasks should
   * be added to the {@link CreationalContext} by this method, only to be called by the bean manager itself, before
   * putting all beans created within the context into service.
   *
   * @param context the {@link CreationalContext} associated with this bean construction.
   * @return a new instance of the bean.
   */
  public T getInstance(CreationalContext context);
}
