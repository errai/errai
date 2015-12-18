/*
 * Copyright (C) 2009 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.cdi.server;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.QueueSession;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;

/**
 * @author Heiko Braun <hbraun@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 */
public class CDIServerUtil {
  @SuppressWarnings("unchecked")
  public static <T> T lookupBean(final BeanManager beanManager,
      final Class<T> serviceType, Annotation... annotations) {
    final Bean<?> bean = beanManager.resolve(beanManager.getBeans(serviceType, annotations));

    if (bean == null) {
      return null;
    }

    return (T) beanManager.getReference(bean, serviceType,
        beanManager.createCreationalContext(bean));
  }

  public static QueueSession getSession(final Message message) {
    return message.getResource(QueueSession.class, "Session");
  }

  @SuppressWarnings("unchecked")
  public static <T> T lookupRPCBean(final BeanManager beanManager,
                                    final Class<?> beanClass,
                                    final Annotation[] annotations) {
    final Bean<?> bean;

    if (annotations != null) {
      bean = beanManager.resolve(beanManager.getBeans(beanClass, annotations));
    }
    else {
      bean = beanManager.resolve(beanManager.getBeans(beanClass));
    }

    return (T) beanManager.getReference(bean, beanClass, beanManager.createCreationalContext(bean));
  }

  static class AnnotationHolder implements Annotation {
    private final Class<? extends Annotation> annotationType;

    AnnotationHolder(final Class<? extends Annotation> annotationType) {
      this.annotationType = annotationType;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return annotationType;
    }

    @Override
    public String toString() {
      return annotationType.getName();
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof AnnotationHolder)) return false;

      final AnnotationHolder that = (AnnotationHolder) o;

      if (annotationType != null ? !annotationType.equals(that.annotationType) : that.annotationType != null)
        return false;

      return true;
    }

    @Override
    public int hashCode() {
      return annotationType != null ? annotationType.hashCode() : 0;
    }
  }
}
