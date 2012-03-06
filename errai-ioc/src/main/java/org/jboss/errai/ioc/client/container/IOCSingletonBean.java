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

/**
 * Represents a bean inside the container, capturing the type, qualifiers and instance reference for the bean.
 *
 * @author Mike Brock
 */
public class IOCSingletonBean<T> extends AbstractIOCBean<T> {
  private final T instance;

  private IOCSingletonBean(Class<T> type, Annotation[] qualifiers, T instance) {
    this.type = type;
    this.qualifiers = new HashSet<Annotation>();
    if (qualifiers != null) {
      Collections.addAll(this.qualifiers, qualifiers);
    }
    this.instance = instance;
  }

  /**
   * Creates a new IOC Bean reference
   *
   * @param type       The type of a bean
   * @param qualifiers The qualifiers of the bean.
   * @param instance   The instance of the bean.
   * @param <T>        The type of the bean
   * @return A new instance of <tt>IOCSingletonBean</tt>
   */
  public static <T> IOCBeanDef<T> newBean(Class<T> type, Annotation[] qualifiers, T instance) {
    return new IOCSingletonBean<T>(type, qualifiers, instance);
  }


  @Override
  public T getInstance(CreationalContext context) {
    return instance;
  }

  public T getInstance() {
    return instance;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IOCSingletonBean)) return false;

    IOCSingletonBean iocBean = (IOCSingletonBean) o;

    if (qualifiers != null ? !qualifiers.equals(iocBean.qualifiers) : iocBean.qualifiers != null) return false;
    if (type != null ? !type.equals(iocBean.type) : iocBean.type != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = type != null ? type.hashCode() : 0;
    result = 31 * result + (qualifiers != null ? qualifiers.hashCode() : 0);
    return result;
  }
}
