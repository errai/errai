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
import java.util.Set;

/**
 * @author Mike Brock
 */
public class IOCBean<T> {
  private final Class<T> type;
  private final Set<Annotation> qualifiers;
  private final T instance;

  public IOCBean(Class<T> type, Annotation[] qualifiers, T instance) {
    this.type = type;
    this.qualifiers = new HashSet<Annotation>();
    Collections.addAll(this.qualifiers, qualifiers);
    this.instance = instance;
  }

  public Class<?> getType() {
    return type;
  }

  public Set<Annotation> getQualifiers() {
    return qualifiers;
  }

  public T getInstance() {
    return instance;
  }
  
  public boolean matches(Set<Annotation> annotations) {
    return qualifiers.containsAll(annotations);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IOCBean)) return false;

    IOCBean iocBean = (IOCBean) o;

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
