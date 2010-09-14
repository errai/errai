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

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Jul 20, 2010
 */
public class AnnotatedMethodWrapper<X> implements AnnotatedMethod<X>
{
  AnnotatedMethod<X> delegate;

  public AnnotatedMethodWrapper(AnnotatedMethod<X> delegate)
  {
    this.delegate = delegate;
  }

  public Method getJavaMember()
  {
    return delegate.getJavaMember();
  }

  public List<AnnotatedParameter<X>> getParameters()
  {
    return delegate.getParameters();
  }

  public boolean isStatic()
  {
    return delegate.isStatic();
  }

  public AnnotatedType<X> getDeclaringType()
  {
    return delegate.getDeclaringType();
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
