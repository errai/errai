/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.cdi.server;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * Class template to override annotation meta data.
 * 
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Jul 20, 2010
 */
public class AnnotatedTypeWrapper<X> implements AnnotatedType<X>
{
  protected AnnotatedType<X> delegate;

  public AnnotatedTypeWrapper(AnnotatedType<X> delegate)
  {
    this.delegate = delegate;
  }

  public Class<X> getJavaClass()
  {
    return delegate.getJavaClass();
  }

  public Set<AnnotatedConstructor<X>> getConstructors()
  {
    return delegate.getConstructors();
  }

  public Set<AnnotatedMethod<? super X>> getMethods()
  {
    return delegate.getMethods();
  }

  public Set<AnnotatedField<? super X>> getFields()
  {
    return delegate.getFields();
  }

  public Type getBaseType()
  {
    return delegate.getBaseType();
  }

  public Set<Type> getTypeClosure()
  {
    return delegate.getTypeClosure();
  }

  public <T extends Annotation> T getAnnotation(Class<T> annotationType)
  {
    return delegate.getAnnotation(annotationType);
  }

  public Set<Annotation> getAnnotations()
  {
    return delegate.getAnnotations();
  }

  public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
  {
    return delegate.isAnnotationPresent(annotationType);
  }
}
