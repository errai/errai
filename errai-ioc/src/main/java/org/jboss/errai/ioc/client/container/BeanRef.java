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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock
 */
public final class BeanRef {
  private final Class<?> clazz;
  private final Set<Annotation> annotations;

  public BeanRef(Class<?> clazz) {
    this.clazz = clazz;
    this.annotations = Collections.emptySet();
  }

  public BeanRef(Class<?> clazz, Annotation[] annotations) {
    this.clazz = clazz;
    this.annotations = new HashSet<Annotation>(wrapAnnotations(Arrays.asList(annotations)));
  }

  public BeanRef(Class<?> clazz, Set<Annotation> annotations) {
    this.clazz = clazz;
    this.annotations = wrapAnnotations(annotations);
  }

  private static Set<Annotation> wrapAnnotations(Collection<Annotation> list) {
    Set<Annotation> annos = new HashSet<Annotation>();
    for (final Annotation a : list) {
      annos.add(new AnnotationHashWapper(a));
    }
    return annos;
  }

  public Class<?> getClazz() {
    return clazz;
  }

  public Annotation[] getAnnotations() {
    return annotations.toArray(new Annotation[annotations.size()]);
  }

  private static class AnnotationHashWapper implements Annotation {
    private final Annotation _delegate;
    private final Class<?> type;

    private AnnotationHashWapper(Annotation _delegate) {
      this._delegate = _delegate;
      this.type = _delegate.annotationType();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof AnnotationHashWapper)) return false;

      AnnotationHashWapper that = (AnnotationHashWapper) o;

      if (type != null ? !type.equals(that.type) : that.type != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return type != null ? type.hashCode() : 0;
    }

    @Override
    public String toString() {
      return _delegate.toString();
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return _delegate.annotationType();
    }
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BeanRef)) return false;

    BeanRef beanRef = (BeanRef) o;

    if (annotations != null ? !annotations.equals(beanRef.annotations) : beanRef.annotations != null) return false;
    if (clazz != null ? !clazz.equals(beanRef.clazz) : beanRef.clazz != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = clazz != null ? clazz.hashCode() : 0;
    result = 31 * result + (annotations != null ? annotations.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "BeanRef{" +
            "clazz=" + clazz +
            ", annotations=" + annotations +
            '}';
  }
}
