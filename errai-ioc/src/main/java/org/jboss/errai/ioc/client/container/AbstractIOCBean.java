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

import org.jboss.errai.ioc.client.QualifierUtil;

/**
 *
 * @author Mike Brock
 */
public abstract class AbstractIOCBean<T> implements IOCBeanDef<T> {
  protected String name;
  protected Class<T> type;
  protected Class<?> beanType;
  protected Set<Annotation> qualifiers;
  protected boolean concrete;

  /**
   * Returns the absolute type of the bean
   *
   * @return a class representing the absolute type of the bean
   */
  @Override
  public Class<T> getType() {
    return type;
  }

  @Override
  public Class<?> getBeanClass() {
    return beanType;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isConcrete() {
    return concrete;
  }

  /**
   * Returns a set of qualifiers associated with this bean
   *
   * @return A set of qualifiers. Returns an empty set if none.
   */
  @Override
  public Set<Annotation> getQualifiers() {
    return qualifiers;
  }

  /**
   * Returns true if the underlying bean contains all of the annotations specified.
   *
   * @param annotations a set of annotations to compare
   * @return true if matches
   */
  @Override
  public boolean matches(final Set<Annotation> annotations) {
    return QualifierUtil.matches(annotations, qualifiers);
  }

  @Override
  public String toString() {
    return getClass().getName().substring(1 + getClass().getName().lastIndexOf('.')) +
            " [name=" + name + ", type=" + type + ", beanType=" + beanType + ", qualifiers=" + qualifiers
            + ", concrete=" + concrete + "]";
  }
}
