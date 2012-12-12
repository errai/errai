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
import java.util.Set;

/**
 * Represents a bean definition within the bean manager.
 *
 * @author Mike Brock
 */
public interface IOCBeanDef<T> {
  /**
   * Returns the type of the bean.
   *
   * @see #getBeanClass()
   * @return the type of the bean.
   */
  public Class<T> getType();


  /**
   * Returns the actual bean class represented by this bean.
   *
   * @return the actual type of the bean.
   */
  public Class<?> getBeanClass();

  /**
   * Returns the scope of the bean.
   *
   * @returns the annotation type representing the scope of the bean.
   */
  public Class<? extends Annotation> getScope();

  /**
   * Returns an instance of the bean within the active scope.
   *
   * @return The bean instance.
   */
  public T getInstance();

  /**
   * Returns an instance of the bean within the active scope, using the specified SimpleCreationalContext.
   *
   * @param context
   * @return
   */
  T getInstance(CreationalContext context);

  /**
   * Returns a new instance of the bean. Calling this method overrides the underlying scope and instantiates a new
   * instance of the bean.
   *
   * @return a new instance of the bean.
   */
  public T newInstance();

  /**
   * Returns any qualifiers associated with the bean.
   * @return
   */
  public Set<Annotation> getQualifiers();


  /**
   * Returns true if the beans qualifiers match the specified set of qualifiers.
   *
   * @param annotations the qualifiers to compare
   * @return returns whether or not the bean matches the set of qualifiers
   */
  public boolean matches(Set<Annotation> annotations);

  /**
   * Returns the name of the bean.
   *
   * @return the name of the bean. If the bean does not have a name, returns null.
   */
  public String getName();

  /**
   * Returns true if the bean is a concrete bean definition and not an interface or abstract type.
   *
   * @return true if concrete.
   */
  public boolean isConcrete();
}
