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

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.ioc.client.QualifierUtil;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is used as a resolver class within the bean manager to represent a unique bean definition
 * for a given type and combination of qualifiers. It provides consistent {@link #equals(Object)} and
 * {@link #hashCode()} functionality for any two instances of <tt>BeanRef</tt>.
 * <p/>
 * For example:
 * <pre>
 *   <code>
 *     BeanRef beanRefA = new BeanRef(String.class, new Annotation[] { new Foo() { ... } });
 *     BeanRef beanRefB = new BeanRef(String.class, new Annotation[] { new Foo() { ... } });
 *     assertTrue(beanRefA.equals(beanRefB)); // should equal true!
 *   </code>
 * </pre>
 *
 * @author Mike Brock
 */
public final class BeanRef {
  private final Class<?> clazz;
  private final Set<Annotation> annotations;
  private final Set<Annotation> hashConsistentAnnotations;

  /**
   * Constructs a new instance of <tt>BeanRef</tt> with the given bean type and qualifiers. Neither the {@param clazz}
   * or the {@param annotation} parameter may be null.
   *
   * @param clazz
   *     the bean type.
   * @param annotations
   *     an array of qualifiers associated with this bean.
   */
  public BeanRef(final Class<?> clazz,
                 final Annotation[] annotations) {
    this.clazz = clazz;
    this.annotations = new HashSet<Annotation>(Arrays.asList(annotations));
    this.hashConsistentAnnotations = wrapAnnotations(this.annotations);
  }

  private static Set<Annotation> wrapAnnotations(final Collection<Annotation> list) {
    final Set<Annotation> annotations = new HashSet<Annotation>();
    for (final Annotation a : list) {
      annotations.add(new AnnotationHashWrapper(a));
    }
    return annotations;
  }

  /**
   * Return the bean type.
   *
   * @return an instance of {@link Class} representing the type of this bean.
   */
  public Class<?> getClazz() {
    return clazz;
  }

  /**
   * Return an array of qualifiers associated with this bean.
   *
   * @return an array of {{@link Annotation}} representing the qualifiers for this bean.
   */
  public Annotation[] getAnnotations() {
    return annotations.toArray(new Annotation[annotations.size()]);
  }

  private static class AnnotationHashWrapper implements Annotation {
    private final Annotation _delegate;
    private final int hashCode;

    private AnnotationHashWrapper(final Annotation _delegate) {
      this._delegate = _delegate;
      this.hashCode = QualifierUtil.hashCodeOf(_delegate);
    }

    @Override
    public boolean equals(final Object o) {
      return this == o || o instanceof AnnotationHashWrapper
          && !(annotationType() != null ?
          !annotationType().equals(((AnnotationHashWrapper) o).annotationType()) :
          ((AnnotationHashWrapper) o).annotationType() != null);
    }

    @Override
    public int hashCode() {
      return hashCode;
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
  public boolean equals(final Object o) {
    return this == o || o instanceof BeanRef
        && !(hashConsistentAnnotations != null ?
        !hashConsistentAnnotations.equals(((BeanRef) o).hashConsistentAnnotations) : ((BeanRef) o).hashConsistentAnnotations != null)
        && !(clazz != null ? !clazz.equals(((BeanRef) o).clazz) : ((BeanRef) o).clazz != null);
  }

  @Override
  public int hashCode() {
    int result = clazz != null ? clazz.hashCode() : 0;
    result = 31 * result + (hashConsistentAnnotations != null ? hashConsistentAnnotations.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return clazz + "[" + annotations + "]";
  }
}
